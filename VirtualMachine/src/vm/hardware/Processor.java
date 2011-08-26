package vm.hardware;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import vm.app.InstructionSet;
import vm.app.SourceLine;

public class Processor {
    
    private static Processor sInstance;
    
    private UiProcessorListener mListener;

    boolean mIsFinished = false;

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
        boolean shouldIncrementPC = true;

        // Se o programa for reinicializado ou outro programa for inicializar
        // Os recursos precisam ser liberados para a nova execucao
        if (mIsFinished) {
            // Reinicia o estado do programa
            Memory.getInstance().cleanMemory();
            Memory.getInstance().cleanComments();
            if (mListener != null) {
                mListener.onRestartProgram();
            }
            
            // O estado da execucao so pode ser resetado aqui
            // para nao gerar inconsistencias
            mIsFinished = false;
        }
        int programCounter = Memory.getInstance().getNextInstructionIndex();
        final SourceLine line = Memory.getInstance().getSourceLine(programCounter);

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
                mIsFinished = true;
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
                boolean inputOk = false;
                int inputValue = 0;
                String stringValue;

                while (!inputOk) {
                    stringValue = JOptionPane.showInputDialog(null, "Qual o valor de entrada?", 
                            "Valor de entrada:", JOptionPane.QUESTION_MESSAGE);
                    try {
                        inputValue = Integer.parseInt(stringValue);
                        inputOk = true;
                    } catch (NumberFormatException ex) {
                        inputOk = false;
                    }
                }
                line.mComment = Memory.getInstance().doRd(inputValue);
                
                final String finalInputString = String.format("%d", inputValue);
                // Atualiza Ui para mostrar entrada
                if (mListener != null) {
                    // Chama o callback do listener na EDT
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onInputEntered(finalInputString);
                        }
                    });
                }
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

        final boolean callFinished = mIsFinished;
        if (mListener != null) {
            // Chama o callback do listener na EDT
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mListener.onInstructionExecuted(line.mLineNumber);
                    if (callFinished) {
                        mListener.onProgramFinished();
                    }
                }
            });
        }
        return mIsFinished;
    }

    // INNER CLASSES
    
    /**
     * Listener para atualizar componentes de interface grafica
     * Roda na EDT
     */
    public static interface UiProcessorListener {
        public void onInstructionExecuted(int lineNumber);
        public void onInputEntered(String input);
        public void onProgramFinished();
        public void onRestartProgram();
    }
}
