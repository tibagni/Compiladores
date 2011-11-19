package comp.app.alg;

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

    public boolean isVarAlreadyDeclared(String lexema) {
        return isAlreadyDeclared(lexema, SymbolTableEntry.TYPE_VARIABLE);
    }

    public boolean isSubRoutineAlreadyDeclared(String lexema) {
        return isAlreadyDeclared(lexema, SymbolTableEntry.TYPE_PROCEDURE,
                SymbolTableEntry.TYPE_FUNCTION);
    }

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

    public void setFunctionAtTopReturnType(int returnType) {
        mSymbolTable.peek().mReturnType = returnType;
    }

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

    public boolean isFunction(int index) {
        if (mSymbolTable.get(index).mType == SymbolTableEntry.TYPE_FUNCTION) {
            return true;
        }
        return false;
    }

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
