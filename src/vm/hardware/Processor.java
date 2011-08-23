package vm.hardware;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import vm.app.InstructionSet;
import vm.app.SourceLine;

public class Processor {
    
    private static Processor sInstance;
    
    private UiProcessorListener mListener;

    private Processor() { }
    
    public static Processor getInstance() {
        if (sInstance == null) {
            sInstance = new Processor();
        }
        return sInstance;
    }

    public void setListener(UiProcessorListener listener) {
        mListener = listener;
    }

    public boolean proccessNextLine() {
        int programCounter = Memory.getInstance().getNextInstructionIndex();
        SourceLine line = Memory.getInstance().getSourceLine(programCounter);

        boolean shouldIncrementPC = true;
        boolean isFinished        = false;

        int insruction = InstructionSet.INSTRUCTION_SET.get(line.mInstruction.toUpperCase());
        switch (insruction) {
            case InstructionSet.LDC:
                // LDC k - Carregar constante
                line.mComment = Memory.getInstance().doLdc(Integer.parseInt(line.mAtt1));
                break;
            case InstructionSet.LDV:
                // LDV n - Carregar valor
                line.mComment = Memory.getInstance().doLdv(Integer.parseInt(line.mAtt1));
                break;
            case InstructionSet.ADD:
                // ADD - Sem atributos
                line.mComment = Memory.getInstance().doAdd();
                break;
            case InstructionSet.SUB:
                // SUB - Sem atributos
                line.mComment = Memory.getInstance().doSub();
                break;
            case InstructionSet.MULT:
                // MULT - Sem atributos
                line.mComment = Memory.getInstance().doMult();
                break;
            case InstructionSet.DIVI:
                // DIVI - Sem atributos
                line.mComment = Memory.getInstance().doDivi();
                break;
            case InstructionSet.INV:
                // INV - Sem atributos
                line.mComment = Memory.getInstance().doInv();
                break;
            case InstructionSet.AND:
                // AND - Sem atributos
                line.mComment = Memory.getInstance().doAnd();
                break;
            case InstructionSet.OR:
                // OR - Sem atributos
                line.mComment = Memory.getInstance().doOr();
                break;
            case InstructionSet.NEG:
                // NEG - Sem atributos
                line.mComment = Memory.getInstance().doNeg();
                break;
            case InstructionSet.CME:
                // CME - Sem atributos
                line.mComment = Memory.getInstance().doCme();
                break;
            case InstructionSet.CMA:
                // CMA - Sem atributos
                line.mComment = Memory.getInstance().doCma();
                break;
            case InstructionSet.CEQ:
                // CEQ - Sem atributos
                line.mComment = Memory.getInstance().doCeq();
                break;
            case InstructionSet.CDIF:
                // CDIF - Sem atributos
                line.mComment = Memory.getInstance().doCdif();
                break;
            case InstructionSet.CMEQ:
                // CMEQ - Sem atributos
                line.mComment = Memory.getInstance().doCmeq();
                break;
            case InstructionSet.CMAQ:
                // CMAQ - Sem atributos
                line.mComment = Memory.getInstance().doCmaq();
                break;
            case InstructionSet.START:
                // START - Sem atributos
                line.mComment = Memory.getInstance().doStart();
                break;
            case InstructionSet.HLT:
                // HLT - Sem atributos
                isFinished = true;
                line.mComment = Memory.getInstance().doHlt();
                break;
            case InstructionSet.STR:
                // STR n
                line.mComment = Memory.getInstance().doStr(Integer.parseInt(line.mAtt1));
                break;
            case InstructionSet.JMP: 
                // JMP t
                shouldIncrementPC = false; // PC sera alterado na instrucao
                line.mComment = Memory.getInstance().doJmp(line.mAtt1);
                break;
            case InstructionSet.JMPF:
                // JMPF t
                shouldIncrementPC = false; // PC sera alterado na instrucao
                line.mComment = Memory.getInstance().doJmpf(line.mAtt1);
                break;
            case InstructionSet.NULL:
                // NULL - Nao faz nada
                break;
            case InstructionSet.RD:
                // RD (VALOR ENTRADO PELO USUARIO)
                // TODO checar consistencia de inteiro
                String valor = JOptionPane.showInputDialog("Entre com o Valor.");
                line.mComment = Memory.getInstance().doRd(Integer.parseInt(valor));
                break;
            case InstructionSet.PRN:
                // PRN - Sem atributos
                line.mComment = Memory.getInstance().doPrn();
                break;
            case InstructionSet.ALLOC:
                // ALLOC m,n
                line.mComment = Memory.getInstance().doAlloc(Integer.parseInt(line.mAtt1),
                        Integer.parseInt(line.mAtt2));
                break;
            case InstructionSet.DALLOC:
                // DALLOC m,n
                line.mComment = Memory.getInstance().doDalloc(Integer.parseInt(line.mAtt1),
                        Integer.parseInt(line.mAtt2));
                break;
            case InstructionSet.CALL:
                // CALL t
                shouldIncrementPC = false; // PC sera alterado na instrucao
                line.mComment = Memory.getInstance().doCall(line.mAtt1);
                break;
            case InstructionSet.RETURN:
                // RETURN - Sem atributos
                shouldIncrementPC = false; // PC sera alterado na instrucao
                line.mComment = Memory.getInstance().doReturn();
                break;
        }
        // Incrementa o program counter
        if (shouldIncrementPC) {
            Memory.getInstance().incProgramCounter();
        }

        if (mListener != null) {
            // Chama o callback do listener na EDT
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mListener.onInstructionExecuted();
                }
            });
        }

        return isFinished;
    }

    // INNER CLASSES
    
    /**
     * Listener para atualizar componentes de interface grafica
     * Roda na EDT
     */
    public static interface UiProcessorListener {
        public void onInstructionExecuted();
    }
}
