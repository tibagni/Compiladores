package vm.hardware;

import java.util.HashMap;
import javax.swing.JOptionPane;

import vm.app.SourceLine;

public class Processor {
    private static final HashMap<String, Integer> INSTRUCTION_SET = new HashMap<String, Integer>();

    private static final int                      LDC             = 0;
    private static final int                      LDV             = 1;
    private static final int                      ADD             = 2;
    private static final int                      SUB             = 3;
    private static final int                      MULT            = 4;
    private static final int                      DIVI            = 5;
    private static final int                      INV             = 6;
    private static final int                      AND             = 7;
    private static final int                      OR              = 8;
    private static final int                      NEG             = 9;
    private static final int                      CME             = 10;
    private static final int                      CMA             = 11;
    private static final int                      CEQ             = 12;
    private static final int                      CDIF            = 13;
    private static final int                      CMEQ            = 14;
    private static final int                      CMAQ            = 15;
    private static final int                      START           = 16;
    private static final int                      HLT             = 17;
    private static final int                      STR             = 18;
    private static final int                      JMP             = 19;
    private static final int                      JMPF            = 20;
    private static final int                      NULL            = 21;
    private static final int                      RD              = 22;
    private static final int                      PRN             = 23;
    private static final int                      ALLOC           = 24;
    private static final int                      DALLOC          = 25;
    private static final int                      CALL            = 26;
    private static final int                      RETURN          = 27;

    static {
        INSTRUCTION_SET.put("LDC", LDC);
        INSTRUCTION_SET.put("LDV", LDV);
        INSTRUCTION_SET.put("ADD", ADD);
        INSTRUCTION_SET.put("SUB", SUB);
        INSTRUCTION_SET.put("MULT", MULT);
        INSTRUCTION_SET.put("DIVI", DIVI);
        INSTRUCTION_SET.put("INV", INV);
        INSTRUCTION_SET.put("AND", AND);
        INSTRUCTION_SET.put("OR", OR);
        INSTRUCTION_SET.put("NEG", NEG);
        INSTRUCTION_SET.put("CME", CME);
        INSTRUCTION_SET.put("CMA", CMA);
        INSTRUCTION_SET.put("CEQ", CEQ);
        INSTRUCTION_SET.put("CDIF", CDIF);
        INSTRUCTION_SET.put("CMEQ", CMEQ);
        INSTRUCTION_SET.put("CMAQ", CMAQ);
        INSTRUCTION_SET.put("START", START);
        INSTRUCTION_SET.put("HLT", HLT);
        INSTRUCTION_SET.put("STR", STR);
        INSTRUCTION_SET.put("JMP", JMP);
        INSTRUCTION_SET.put("JMPF", JMPF);
        INSTRUCTION_SET.put("NULL", NULL);
        INSTRUCTION_SET.put("RD", RD);
        INSTRUCTION_SET.put("PRN", PRN);
        INSTRUCTION_SET.put("ALLOC", ALLOC);
        INSTRUCTION_SET.put("DALLOC", DALLOC);
        INSTRUCTION_SET.put("CALL", CALL);
        INSTRUCTION_SET.put("RETURN", RETURN);
    }

    public static boolean proccessLine(SourceLine line) {
        boolean shouldIncrementPC = true;
        boolean isFinished        = false;

        int insruction = INSTRUCTION_SET.get(line.mInstruction.toUpperCase());
        switch (insruction) {
            case LDC:
                // LDC k - Carregar constante
                line.mComment = Memory.getInstance().doLdc(Integer.parseInt(line.mAtt1));
                break;
            case LDV:
                // LDV n - Carregar valor
                line.mComment = Memory.getInstance().doLdv(Integer.parseInt(line.mAtt1));
                break;
            case ADD:
                // ADD - Sem atributos
                line.mComment = Memory.getInstance().doAdd();
                break;
            case SUB:
                // SUB - Sem atributos
                line.mComment = Memory.getInstance().doSub();
                break;
            case MULT:
                // MULT - Sem atributos
                line.mComment = Memory.getInstance().doMult();
                break;
            case DIVI:
                // DIVI - Sem atributos
                line.mComment = Memory.getInstance().doDivi();
                break;
            case INV:
                // INV - Sem atributos
                line.mComment = Memory.getInstance().doInv();
                break;
            case AND:
                // AND - Sem atributos
                line.mComment = Memory.getInstance().doAnd();
                break;
            case OR:
                // OR - Sem atributos
                line.mComment = Memory.getInstance().doOr();
                break;
            case NEG:
                // NEG - Sem atributos
                line.mComment = Memory.getInstance().doNeg();
                break;
            case CME:
                // CME - Sem atributos
                line.mComment = Memory.getInstance().doCme();
                break;
            case CMA:
                // CMA - Sem atributos
                line.mComment = Memory.getInstance().doCma();
                break;
            case CEQ:
                // CEQ - Sem atributos
                line.mComment = Memory.getInstance().doCeq();
                break;
            case CDIF:
                // CDIF - Sem atributos
                line.mComment = Memory.getInstance().doCdif();
                break;
            case CMEQ:
                // CMEQ - Sem atributos
                line.mComment = Memory.getInstance().doCmeq();
                break;
            case CMAQ:
                // CMAQ - Sem atributos
                line.mComment = Memory.getInstance().doCmaq();
                break;
            case START:
                // START - Sem atributos
                line.mComment = Memory.getInstance().doStart();
                break;
            case HLT:
                // HLT - Sem atributos
                isFinished = true;
                line.mComment = Memory.getInstance().doHlt();
                break;
            case STR:
                // STR n
                line.mComment = Memory.getInstance().doStr(Integer.parseInt(line.mAtt1));
                break;
            case JMP: 
                // JMP t
                shouldIncrementPC = false; // PC sera alterado na instrucao
                line.mComment = Memory.getInstance().doJmp(Integer.parseInt(line.mAtt1));
                break;
            case JMPF:
                // JMPF t
                shouldIncrementPC = false; // PC sera alterado na instrucao
                line.mComment = Memory.getInstance().doJmpf(Integer.parseInt(line.mAtt1));
                break;
            case NULL:
                // NULL - Nao faz nada
                break;
            case RD:
                // RD (VALOR ENTRADO PELO USUARIO)
                // TODO checar consistencia de inteiro
                String valor = JOptionPane.showInputDialog("Entre com o Valor.");
                line.mComment = Memory.getInstance().doRd(Integer.parseInt(valor));
                break;
            case PRN:
                // PRN - Sem atributos
                line.mComment = Memory.getInstance().doPrn();
                break;
            case ALLOC:
                // ALLOC m,n
                line.mComment = Memory.getInstance().doAlloc(Integer.parseInt(line.mAtt1),
                        Integer.parseInt(line.mAtt2));
                break;
            case DALLOC:
                // DALLOC m,n
                line.mComment = Memory.getInstance().doDalloc(Integer.parseInt(line.mAtt1),
                        Integer.parseInt(line.mAtt2));
                break;
            case CALL:
                // CALL t
                shouldIncrementPC = false; // PC sera alterado na instrucao
                line.mComment = Memory.getInstance().doCall(Integer.parseInt(line.mAtt1));
                break;
            case RETURN:
                // RETURN - Sem atributos
                shouldIncrementPC = false; // PC sera alterado na instrucao
                line.mComment = Memory.getInstance().doReturn();
                break;
        }
        // Incrementa o program counter
        if (shouldIncrementPC) {
            Memory.getInstance().incProgramCounter();
        }
        return isFinished;
    }
}
