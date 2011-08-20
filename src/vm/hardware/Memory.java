package vm.hardware;

import java.util.ArrayList;

import vm.app.SourceLine;

public class Memory {
    public static Memory sInstance;
    
    // Lista que armazena o programa (codigo fonte)
    private ArrayList<SourceLine> mSourceCodeMemory;
    // Program Counter - Aponta endereco da proxima instrucao
    private int mProgramCounter;
    
    // Vetor da memoria
    private int[] mMemoryStack;
    // Topo da pilha (memoria)
    private int mTop;
    
    private static final int MEMORY_SIZE = 1024;

    private static final int TRUE  = 1;
    private static final int FALSE = 0;

    private Memory() {
        mMemoryStack = new int[MEMORY_SIZE];
        mTop = -1; // Primeira posicao e 0. Inicialmente a memoria esta vazia (-1)
        mSourceCodeMemory = new ArrayList<SourceLine>();
        mProgramCounter = 0;
    }

    public static Memory getInstance() {
        if (sInstance == null) {
            sInstance = new Memory();
        }
        return sInstance;
    }

    public int getTop() {
        return mTop;
    }

    public int getData(int position) {
        if (position < 0 || position > (mTop + 1)) {
            throw new ArrayIndexOutOfBoundsException("Posicao nao existe na memoria");
        }
        return mMemoryStack[position];
    }

    /* CODIGO FONTE */
    
    /* package */ synchronized void addSourceLine(SourceLine line) {
        mSourceCodeMemory.add(line);
    }

    public synchronized SourceLine getSourceLine(int pos) {
        return mSourceCodeMemory.get(pos);
    }
    
    public synchronized int getSourceLineCount() {
        return mSourceCodeMemory.size();
    }

    public void incProgramCounter() {
        mProgramCounter++;
    }

    public synchronized int getNextInstructionIndex() {
        return mProgramCounter;
    }

    /* INSTRUCOES */

    public String doStart() {
        mTop = -1;
        return "S:=-1";
    }

    /**
     * Carrega constante para o topo da pilha
     * 
     * @param k Constante a ser carregada
     * @return Comentario
     */
    public String doLdc(int k) {
        mTop++;
        mMemoryStack[mTop] = k;
        
        return "S:=s+1; M[s]:=k";
    }
    
    /**
     * Carrega valor para o topo da pilha
     * 
     * @param n Endereco do valor a ser carregado
     * @return Comentario
     */
    public String doLdv(int n) {
        mTop++;
        mMemoryStack[mTop] = mMemoryStack[n];
        
        return "S:=s+1; M[s]:=M[n]";
    }
    
    /**
     * Soma os valores do topo e topo-1 da pilha
     * e armazena o resultado em topo-1
     * 
     * @return Comentario
     */
    public String doAdd() {
        mMemoryStack[mTop - 1] = mMemoryStack[mTop - 1] + mMemoryStack[mTop];
        mTop--;
        
        return "M[s-1]:=M[s-1]+M[s]; s:=s-1";
    }

    /**
     * Subtrai os valores do topo e topo-1 da pilha
     * e armazena o resultado em topo-1
     * 
     * @return Comentario
     */
    public String doSub() {
        mMemoryStack[mTop - 1] = mMemoryStack[mTop - 1] - mMemoryStack[mTop];
        mTop--;
        
        return "M[s-1]:=M[s-1]-M[s]; s:=s-1";
    }

    /**
     * Multiplica os valores do topo e topo-1 da pilha
     * e armazena o resultado em topo-1
     * 
     * @return Comentario
     */
    public String doMult() {
        mMemoryStack[mTop - 1] = mMemoryStack[mTop - 1] * mMemoryStack[mTop];
        mTop--;
        
        return "M[s-1]:=M[s-1]*M[s]; s:=s-1";
    }

    /**
     * Divide os valores do topo e topo-1 da pilha
     * e armazena o resultado em topo-1
     * 
     * @return Comentario
     */
    public String doDivi() {
        mMemoryStack[mTop - 1] = mMemoryStack[mTop - 1] / mMemoryStack[mTop];
        mTop--;
        
        return "M[s-1]:=M[s-1] div M[s]; s:=s-1";
    }

    /**
     * Inverte o sinal do valor armazenado no topo da pilha
     * 
     * @return Comentario
     */
     public String doInv() {
        mMemoryStack[mTop] = -mMemoryStack[mTop];

        return "M[s]:=-M[s]";
    }

     /**
      * Conjuncao
      * 
      * @return Comentario
      */
     public String doAnd() {
        if(mMemoryStack[mTop - 1] == TRUE && mMemoryStack[mTop] == TRUE)
            mMemoryStack[mTop - 1] = TRUE;

        else
            mMemoryStack[mTop - 1] = FALSE;

        mTop--;

        return "se M[s-1]=1 e M[s]=1  então M[s-1]:=1  senão M[s-1]:=0;  s:=s-1";
    }

     /**
      * Disjuncao
      * 
      * @return Comentario
      */
    public String doOr() {
        if(mMemoryStack[mTop - 1] == TRUE || mMemoryStack[mTop] == TRUE)
            mMemoryStack[mTop - 1] = TRUE;

        else
            mMemoryStack[mTop - 1] = FALSE;

        mTop--;

        return "se M[s-1]=1  ou M[s]=1  então M[s-1]:=1  senão M[s-1]:=0; s:=s-1";
    }

    /**
     * Nega o valor logico armazenado no topo da pilha
     * 
     * @return Comentario
     */
    public String doNeg() {
        mMemoryStack[mTop] = 1 - mMemoryStack[mTop];

        return "M[s]:=1-M[s]";
    }

    /**
     * Compara topo e topo-1
     * se topo-1 menor armazena 1 (true) em topo-1
     * senao armazena 0 (false)
     * 
     * @return Comentario
     */
    public String doCme() {
        if(mMemoryStack[mTop - 1] < mMemoryStack[mTop])
            mMemoryStack[mTop - 1] = TRUE;

        else
            mMemoryStack[mTop - 1] = FALSE;

        mTop--;
        
        return "se M[s-1]<M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s-1";
    }
    /**
     * Compara topo e topo-1
     * se topo-1 maior armazena 1 (true) em topo-1
     * senao armazena 0 (false)
     * 
     * @return Comentario
     */
    public String doCma() {
        if(mMemoryStack[mTop - 1] > mMemoryStack[mTop])
            mMemoryStack[mTop - 1] = TRUE;

        else
            mMemoryStack[mTop - 1] = FALSE;

        mTop--;

        return "se M[s-1]>M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s-1";
    }
    /**
     * Compara topo e topo-1
     * se forem iguais armazena 1 (true) em topo-1
     * senao armazena 0 (false)
     * 
     * @return Comentario
     */
    public String doCeq() {
        if(mMemoryStack[mTop - 1] == mMemoryStack[mTop])
            mMemoryStack[mTop - 1] = TRUE;

        else
            mMemoryStack[mTop - 1] = FALSE;

        mTop--;

        return "se M[s-1]=M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s-1";
    }
    /**
     * Compara topo e topo-1
     * se forem diferentes armazena 1 (true) em topo-1
     * senao armazena 0 (false)
     * 
     * @return Comentario
     */
    public String doCdif() {
        if(mMemoryStack[mTop - 1] != mMemoryStack[mTop])
            mMemoryStack[mTop - 1] = TRUE;

        else
            mMemoryStack[mTop - 1] = FALSE;

        mTop--;

        return "se M[s-1] != M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s-1";
    }
    /**
     * Compara topo e topo-1
     * se topo-1 menor igual armazena 1 (true) em topo-1
     * senao armazena 0 (false)
     * 
     * @return Comentario
     */
    public String doCmeq() {
        if(mMemoryStack[mTop - 1] <= mMemoryStack[mTop])
            mMemoryStack[mTop - 1] = TRUE;

        else
            mMemoryStack[mTop - 1] = FALSE;

        mTop--;

        return "se M[s-1] <= M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s-1";
    }
    /**
     * Compara topo e topo-1
     * se topo-1 maior igual armazena 1 (true) em topo-1
     * senao armazena 0 (false)
     * 
     * @return Comentario
     */
    public String doCmaq() {
        if(mMemoryStack[mTop - 1] >= mMemoryStack[mTop])
            mMemoryStack[mTop - 1] = TRUE;

        else
            mMemoryStack[mTop - 1] = FALSE;

        mTop--;

        return "se M[s-1] >= M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s-1";
    }

    /**
     * Termina o programa
     * 
     * @return Comentario
     */
    public String doHlt() {
        return "Fim do Programa";
    }

    /**
     * Armazena valor do topo da pilha em n
     * 
     * @param n Endereco para armazenar o valor
     * @return Comentario
     */
    public String doStr(int n) {
        mMemoryStack[n] = mMemoryStack[mTop];
        mTop--;

        return "M[n]:=M[s]; s:=s-1";
    }


    /**
     * Armazena valor lido no topo da pilha
     * 
     * @param n Valor lido
     * @return Comentario
     */
    public String doRd(int n) {
        mTop++;
        mMemoryStack[mTop] = n;

        return "S:=s+1; M[s]:=próximo valor de entrada.";
    }

    /**
     * Desvia execucao do programa para a posicao t
     * 
     * @param t Posicao
     * @return Comentario
     */
    public String doJmp(int t) {
        mProgramCounter = t;

        return "i:= t";
    }


    /**
     * Mostra Valor armazenado no topo da pilha
     * 
     * @return Comentario
     */
    public String doPrn() {
        mTop--;
        // TODO jogar valor na saida padrao
        return "Imprimir M[s]; s:=s-1";
    }

    /**
     * Aloca memoria
     * 
     * @param m Posicao inicial dos valores a serem armazenados
     * @param n Posicoes a serem alocadas
     * @return Comentario
     */
    public String doAlloc(int m, int n) {
        for(int k = 0; k < n-1; k++){
           mTop++;
           mMemoryStack[mTop] = mMemoryStack[m + k];
        }
        return " - ";
    }

    /**
     * Desaloca memoria
     * 
     * @param m Posicao inicial dos valores armazenados
     * @param n Posicoes a serem desalocadas
     * @return Comentario
     */
    public String doDalloc(int m, int n) {
        for(int k = n-1; k >= 0; k--){
            mMemoryStack[m + k] = mMemoryStack[mTop];
            mTop++;
        }

        return " - ";
    }

    /**
     * Retorno de funcao
     * 
     * @return Comentario
     */
    public String doReturn() {
        mProgramCounter = mMemoryStack[mTop];
        mTop--;

        return "i:=M[s]; s:=s-1";
    }

    /**
     * Chamada de funcao
     * 
     * @param t Endereco da funcao
     * @return Comentario
     */
    public String doCall(int t) {
        mTop++;
        mMemoryStack[mTop] = mProgramCounter + 1;
        mProgramCounter = t;

        return "S:=s+1; M[s]:=i+1; i:=t";
    }

    /**
     * Jump condicional (se falso)
     * 
     * @param t Endereco em que o programa sera desviado
     * @returnComentario
     */
    public String doJmpf(int t) {
        if (mMemoryStack[mTop] == FALSE) {
            mProgramCounter = t;
        } else {
            incProgramCounter();
        }

        return " - ";
    }

}
