package vm.hardware;

import java.util.ArrayList;

import vm.app.SourceLine;

public class Memory {
    public static Memory sInstance;
    
    // Lista que armazena o programa (codigo fonte)
    private ArrayList<SourceLine> mSourceCodeMemory;
    
    // Vetor da memoria
    private int[] mMemoryStack;
    // Topo da pilha (memoria)
    private int mTop;
    
    private static final int MEMORY_SIZE = 128;

    private Memory() {
        mMemoryStack = new int[MEMORY_SIZE];
        mTop = -1; // Primeira posicao e 0. Inicialmente a memoria esta vazia (-1)
        mSourceCodeMemory = new ArrayList<SourceLine>();
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

    /* INSTRUCOES */
    public String start() {
        mTop = -1;
        return "S:=-1";
    }

    public String ldc(int k) {
        mTop++;
        mMemoryStack[mTop] = k;
        
        return "S:=s+1; M[s]:=k";
    }
    
    public String ldv(int n) {
        mTop++;
        mMemoryStack[mTop] = mMemoryStack[n];
        
        return "S:=s+1; M[s]:=M[n]";
    }
    
    public String add() {
        mMemoryStack[mTop - 1] = mMemoryStack[mTop - 1] + mMemoryStack[mTop];
        mTop--;
        
        return "M[s-1]:=M[s-1]+M[s]; s:=s-1";
    }
    
    public String sub() {
        mMemoryStack[mTop - 1] = mMemoryStack[mTop - 1] - mMemoryStack[mTop];
        mTop--;
        
        return "M[s-1]:=M[s-1]-M[s]; s:=s-1";
    }
    
    public String mult() {
        mMemoryStack[mTop - 1] = mMemoryStack[mTop - 1] * mMemoryStack[mTop];
        mTop--;
        
        return "M[s-1]:=M[s-1]*M[s]; s:=s-1";
    }
    
    public String divi() {
        mMemoryStack[mTop - 1] = mMemoryStack[mTop - 1] / mMemoryStack[mTop];
        mTop--;
        
        return "M[s-1]:=M[s-1] div M[s]; s:=s-1";
    }
}
