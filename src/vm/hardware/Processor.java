package vm.hardware;

import java.util.HashMap;

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

    public static void proccessLine(SourceLine line) {
        int insruction = INSTRUCTION_SET.get(line.mInstruction.toUpperCase());
        switch (insruction) {
            case LDC:
                // LDC k
                line.mComment = Memory.getInstance().ldc(Integer.parseInt(line.mAtt1));
                break;
            case LDV:
                // LDV n
                line.mComment = Memory.getInstance().ldv(Integer.parseInt(line.mAtt1));
                break;
            case ADD:
                // ADD - Sem atributos
                line.mComment = Memory.getInstance().add();
                break;
            case SUB:
                // SUB - Sem atributos
                line.mComment = Memory.getInstance().sub();
                break;
            case MULT:
                // MULT - Sem atributos
                line.mComment = Memory.getInstance().mult();
                break;
            case DIVI:
                // DIVI - Sem atributos
                line.mComment = Memory.getInstance().divi();
                break;
            case INV:
                break;
            case AND:
                break;
            case OR:
                break;
            case NEG:
                break;
            case CME:
                break;
            case CMA:
                break;
            case CEQ:
                break;
            case CDIF:
                break;
            case CMEQ:
                break;
            case CMAQ:
                break;
            case START:
                line.mComment = Memory.getInstance().start();
                break;
            case HLT:
                break;
            case STR:
                break;
            case JMP:
                break;
            case JMPF:
                break;
            case NULL:
                break;
            case RD:
                break;
            case PRN:
                break;
            case ALLOC:
                break;
            case DALLOC:
                break;
            case CALL:
                break;
            case RETURN:
                break;
        }
    }
}
