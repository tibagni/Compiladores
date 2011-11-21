package comp.app.alg;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

public class Semantic {

    private Stack<SymbolTableEntry> mSymbolTable;

    private static Semantic sInstance;

    private Semantic() {
        mSymbolTable = new Stack<SymbolTableEntry>();
    }

    public static Semantic getInstance() {
        if (sInstance == null) {
            sInstance = new Semantic();
        }
        return sInstance;
    }

    public void pushToSymbolTable(SymbolTableEntry entry) {
        mSymbolTable.push(entry);
    }

    private boolean isAlreadyDeclared(String lexema, int... type) {
        ListIterator<SymbolTableEntry> it = mSymbolTable.listIterator(mSymbolTable.size() - 1);
        SymbolTableEntry entry = mSymbolTable.get(mSymbolTable.size() - 1);
        // Verifica o topo da pilha primeiro
        if (lexema.equals(entry.mLexema) && contains(type, entry.mType)) {
            return true;
        } else if (entry.mLevel == SymbolTableEntry.SCOPE_MARK) {
            // Se o topo ja for a marca de escopo, entao nao e duplicado.
            return false;
        }
        // Analisa o restante da pilha ate a primeira marca de esopo
        while (it.hasPrevious()) {
            entry = it.previous();
            if (entry.mLevel == SymbolTableEntry.SCOPE_MARK) break;
            if (lexema.equals(entry.mLexema) && contains(type, entry.mType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se um elemento (inteiro) esta presente em um vetor
     *
     * @param array Vetor de inteiros
     * @param element inteiro para verificar se esta presente no vetor
     * @return true se o elemento esta presente ou falso caso contrario
     */
    private boolean contains(int[] array, int element) {
        if (array == null) return false;

        for (int t : array) {
            if (t == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retorna se uma variavel ja foi declarada (levando em conta o escopo)
     * (usado para saber se a rotina que esta sendo declarada e duplicada)
     *
     * @param lexema
     * @return
     */
    public boolean isVarAlreadyDeclared(String lexema) {
        return isAlreadyDeclared(lexema, SymbolTableEntry.TYPE_VARIABLE);
    }

    /**
     * Retorna se uma sub-rotina ja foi declarada (levando em conta o escopo)
     * (usado para saber se a rotina que esta sendo declarada e duplicada)
     *
     * @param lexema
     * @return
     */
    public boolean isSubRoutineAlreadyDeclared(String lexema) {
        return isAlreadyDeclared(lexema, SymbolTableEntry.TYPE_PROCEDURE,
                SymbolTableEntry.TYPE_FUNCTION);
    }

    /**
     * A partir do topo da tabela de simbolos, preenche o campo de todas as variaves
     * que ainda nao possuem um tipo valido.
     *
     * @param varType Tipo de dados para as variaveis sem tipo.
     */
    public void populateVarType(int varType) {
        SymbolTableEntry entry = mSymbolTable.get(mSymbolTable.size() - 1);
        if (entry.mType == SymbolTableEntry.TYPE_VARIABLE &&
                entry.mVarType == SymbolTableEntry.VAR_TYPE_NOT_INITIALIZED) {
            entry.mVarType = varType;
        } else {
            return;
        }

        ListIterator<SymbolTableEntry> it = mSymbolTable.listIterator(mSymbolTable.size() - 1);
        while (it.hasPrevious()) {
            entry = it.previous();
            if (entry.mType != SymbolTableEntry.TYPE_VARIABLE) return;
            if (entry.mVarType != SymbolTableEntry.VAR_TYPE_NOT_INITIALIZED) return;
            entry.mVarType = varType;
        }
    }

    /**
     * Seta o tipo de retorno da funcao cuja a entrada esta no topo da tabela de simbolos
     * @param returnType Tipo de retorno da funcao
     */
    public void setFunctionAtTopReturnType(int returnType) {
        mSymbolTable.peek().mReturnType = returnType;
    }

    /**
     * Desempilha tudo da tabela de simbolos ate a primeira marca de escopo
     * (Deve ser utilizado depois da compilacao de procedimentos e funcoes)
     */
    public void popEverythingUntilScopeMark() {
        SymbolTableEntry entry;
        while (true) {
            entry = mSymbolTable.peek();
            if (entry.mLevel == SymbolTableEntry.SCOPE_MARK) {
                // Remove a SCOPE_MARK
                entry.mLevel = 0;
                break;
            }
            // Remove o objeto do topo da pilha
            mSymbolTable.pop();
        }
    }

    /**
     * Retorna o primeiro indice encontrado para uma entrada na tabela
     * cujo o lexema seja igual ao passado por argumento
     *
     * @param lexema
     * @return Primeiro indice encontrado para o lexema
     */
    public int getFirstIndexOf(String lexema) {
        // Primeiro, verifica se esta logo no topo da pilha
        if (lexema.equals(mSymbolTable.peek().mLexema)) {
            return (mSymbolTable.size() - 1);
        }
        ListIterator<SymbolTableEntry> it = mSymbolTable.listIterator(mSymbolTable.size() - 1);
        SymbolTableEntry entry;
        while (it.hasPrevious()) {
            entry = it.previous();
            if (lexema.equals(entry.mLexema)) {
                return (it.previousIndex() + 1);
            }
        }
        return -1;
    }

    /**
     * Dado um indice na tabela de simbolos, retorna se a entrada e referenta
     * a uma funcao
     *
     * @param index Indice na tabela de simbolos
     * @return true se a entrada for referente a uma funcao, false caso contrario
     */
    public boolean isFunction(int index) {
        if (mSymbolTable.get(index).mType == SymbolTableEntry.TYPE_FUNCTION) {
            return true;
        }
        return false;
    }

    /**
     * Transforma uma expressao in-fixa para a forma posfixa.
     * Por exemplo: (A+B)*C -> AB+C*
     *
     * A expressao de entrada deve ser um vetor de String
     * representando-a na forma in-fixa. Seus elementos sao:
     * (em ordem de precedencia)
     * <br>
     * <ul>
     *  <li>String representando inteiro, booleano
     *  ou identificador (variavel ou funcao)</li>
     *  <br><br>
     *  <li>Operadores unarios:
     *      <ul>
     *          <li>nao - Operador unario de negacao (nao verdadeiro = falso)</li>
     *          <li>+u - Operador unario positivo (+1, +5)</li>
     *          <li>-u - Operador unario negativo (-1, -5)</li>
     *      </ul>
     *  </li>
     *  <li>Operadores aritimeticos
     *      <ul>
     *          <li>div - divisao entre dois inteiros</li>
     *          <li>* - multiplicacao entre dois inteiros</li>
     *          <li>+ - soma entre dois inteiros</li>
     *          <li>- - subtracao entre dois inteiros</li>
     *      </ul>
     *  </li>
     *  <li>Operadores relacionais
     *      <ul>
     *          <li>> - maior que (entre dois inteiros)</li>
     *          <li>< - menor que (entre dois inteiros)</li>
     *          <li>>= - maior ou igual a (entre dois inteiros)</li>
     *          <li><= - menor ou igual a (entre dois inteiros)</li>
     *          <li>= - igual a (entre dois inteiros)</li>
     *      </ul>
     *  </li>
     *  <li>Operadores logicos
     *      <ul>
     *          <li>e - e logico entre dois booleanos</li>
     *          <li>ou - ou logico entre dois booleanos</li>
     *      </ul>
     *  </li>
     * </ul>
     *
     * @param in Vetor contendo a expressao in-fixa
     * @return Vetor contendo a expressao em pos-fixa
     */
    public String[] expressionToPostFix(String[] in) {
        List<String> post = new ArrayList<String>();
        Stack<String> stack = new Stack<String>();

        for (String element : in) {
            if (isOperator(element)) {
                // Verifica precedencia se o topo tambem for um operando
                if (stack.size() > 0 && isOperator(stack.peek())) {
                    String poppedElement;
                    while (stack.size() > 0 && (calculatePriorities(stack.peek(), element) < 0)) {
                        poppedElement = stack.pop();
                        post.add(poppedElement);
                    }
                }
                stack.push(element);
            } else if ("(".equals(element)) {
                stack.push(element);
            } else if (")".equals(element)) {
                String poppedElement = null;
                while (stack.size() > 0 && !("(".equals(stack.peek()))) {
                    poppedElement = stack.pop();
                    post.add(poppedElement);
                }
                stack.pop();
            } else {
                // Aqui o elemento e uma variavel, funcao, valor booleano ou valor inteiro
                post.add(element);
            }
        }
        // Verifica se sobrou algo na pilha para desempilhar!
        String poppedElement;
        while (stack.size() > 0) {
            poppedElement = stack.pop();
            post.add(poppedElement);
        }

        return post.toArray(new String[post.size()]);
    }

    /**
     * Calcula a prioridade entre os operadores em uma expressao
     * <br><br>
     * Ordem de prioridades:
     * <br>
     * <i>Crescente - quanto maior o numero maior a prioridade</i><br>
     * <i>'nao' tem maior prioridade, 'ou' tem menor prioridade</i>
     * <ol>
     *  <li>ou</li>
     *  <li>e</li>
     *  <li>=</li>
     *  <li><=</li>
     *  <li>>=</li>
     *  <li><</li>
     *  <li>></li>
     *  <li>-</li>
     *  <li>+</li>
     *  <li>*</li>
     *  <li>div</li>
     *  <li>-u</li>
     *  <li>+u</li>
     *  <li>nao</li>
     * </ol>
     *
     * @param op1 Operador 1
     * @param op2 Operador 2
     * @return -1 se op1 tiver maior prioridade, 1 se op2 tiver maior prioridade
     *  ou zero se as prioridades forem iguais
     */
    private int calculatePriorities(String op1, String op2) {
        final String[] priorities = {"ou", "e", "=", "<=", ">=", "<", ">",
                                     "-", "+", "*", "div", "-u", "+u", "nao"};

        if (op1 == null || op2 == null) {
            throw new IllegalArgumentException("Os argumentos nao podem ser nulos");
        }

        // Vamos garantir que os argumentos estao em lower case
        op1 = op1.toLowerCase();
        op2 = op2.toLowerCase();

        int priority1 = -1;
        int priority2 = -1;

        for (int i = 0; i < priorities.length; i++) {
            if (op1.equals(priorities[i])) {
                priority1 = i;
            }
            if (op2.equals(priorities[i])) {
                priority2 = i;
            }

            // Se as duas prioridades ja foram encontradas
            // nao ha porque continuar percorrendo o vetor
            if (priority1 != -1 && priority2 != -1) break;
        }

        if (priority1 == priority2) return 0;

        if (priority1 > priority2) {
            return -1;
        } else {
            return 1;
        }

    }

    private boolean isOperator(String element) {
        if (element != null) element = element.toLowerCase();
        if ("nao".equals(element) || "+u".equals(element) || "-u".equals(element) ||
                "div".equals(element) || "*".equals(element) ||
                "+".equals(element) || "-".equals(element) || ">".equals(element) ||
                "<".equals(element) || ">=".equals(element) ||
                "<=".equals(element) || "=".equals(element) || "e".equals(element)||
                "ou".equals(element)) {
            return true;
        }
        return false;
    }

    /**
     * Representa uma entrada na tabela de simbolos
     */
    public static class SymbolTableEntry {
        public static final int TYPE_PROGRAM_NAME = 10;
        public static final int TYPE_VARIABLE = 11;
        public static final int TYPE_PROCEDURE = 12;
        public static final int TYPE_FUNCTION = 13;

        public static final int SCOPE_MARK = 999;

        public static final int VAR_TYPE_NOT_INITIALIZED = -1;
        public static final int RETURN_TYPE_NOT_INITIALIZED = -1;

        /** Endereco de VARIAVEIS */
        public int mAddress;

        /** Endereco de funcoes e procedimentos */
        public int mLabel;

        public String mLexema;
        public int mLevel;
        public int mType;

        /** Se o simbolo for uma variavel, este campo serve para indicar o tipo
         * inteiro ou booleano (Symbols.INTEIRO ou Symbols.BOOLEANO)
         */
        public int mVarType;

         /** Se o simbolo for uma funcao, este campo ira conter o tipo de retorno
          *  inteiro ou booleano (Symbols.INTEIRO ou Symbols.BOOLEANO)
          */
        public int mReturnType;

        public SymbolTableEntry() {
            mVarType = VAR_TYPE_NOT_INITIALIZED;
            mReturnType = RETURN_TYPE_NOT_INITIALIZED;
            mLevel = 0;
        }
    }
}
