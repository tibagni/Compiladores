package vm.app;

import java.util.HashMap;

public class InstructionSet {
    public static final HashMap<String, Integer> INSTRUCTION_SET = new HashMap<String, Integer>();

    public static final int                      LDC             = 0;
    public static final int                      LDV             = 1;
    public static final int                      ADD             = 2;
    public static final int                      SUB             = 3;
    public static final int                      MULT            = 4;
    public static final int                      DIVI            = 5;
    public static final int                      INV             = 6;
    public static final int                      AND             = 7;
    public static final int                      OR              = 8;
    public static final int                      NEG             = 9;
    public static final int                      CME             = 10;
    public static final int                      CMA             = 11;
    public static final int                      CEQ             = 12;
    public static final int                      CDIF            = 13;
    public static final int                      CMEQ            = 14;
    public static final int                      CMAQ            = 15;
    public static final int                      START           = 16;
    public static final int                      HLT             = 17;
    public static final int                      STR             = 18;
    public static final int                      JMP             = 19;
    public static final int                      JMPF            = 20;
    public static final int                      NULL            = 21;
    public static final int                      RD              = 22;
    public static final int                      PRN             = 23;
    public static final int                      ALLOC           = 24;
    public static final int                      DALLOC          = 25;
    public static final int                      CALL            = 26;
    public static final int                      RETURN          = 27;

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

}
