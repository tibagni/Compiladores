package comp.app.alg;

import java.util.ArrayList;

public class CodeGenerator {
    private ArrayList<String> mCode;
    private static CodeGenerator sInstance;
    private boolean finished;

    public static synchronized CodeGenerator getInstance() {
        if (sInstance == null) {
            sInstance = new CodeGenerator();
        }
        return sInstance;
    }

    public synchronized void reset() {
        mCode.clear();
        finished = false;
    }

    private CodeGenerator() {
        finished = false;
        mCode = new ArrayList<String>(200);
    }

    public int appendCode(String code) {
        if (finished) {
            throw new IllegalArgumentException("Voce nao pode inserir " +
            		"em um codigo concluido");
        }
        mCode.add(code);
        mCode.add("\n");
        return mCode.indexOf(code);
    }

    public void modifyCode(int position, String newCode) {
        if (finished) {
            throw new IllegalArgumentException("Voce nao pode modificar " +
                    "um codigo concluido");
        }
        mCode.remove(position);
        mCode.add(position, newCode);
    }

    public void putCodeAt(int position, String code) {
        mCode.add(position, code);
        mCode.add(position + 1, "\n");
    }

    public void generateOperationCode(String op) {
        if ("+".equalsIgnoreCase(op)) {
            appendCode("ADD");
        } else if ("-".equalsIgnoreCase(op)) {
            appendCode("SUB");
        } else if ("*".equalsIgnoreCase(op)) {
            appendCode("MULT");
        } else if ("div".equalsIgnoreCase(op)) {
            appendCode("DIVI");
        } else if ("e".equalsIgnoreCase(op)) {
            appendCode("AND");
        } else if ("ou".equalsIgnoreCase(op)) {
            appendCode("OR");
        } else if ("<".equalsIgnoreCase(op)) {
            appendCode("CME");
        } else if (">".equalsIgnoreCase(op)) {
            appendCode("CMA");
        } else if ("=".equalsIgnoreCase(op)) {
            appendCode("CEQ");
        } else if ("!=".equalsIgnoreCase(op)) {
            appendCode("CDIF");
        } else if ("<=".equalsIgnoreCase(op)) {
            appendCode("CMEQ");
        } else if (">=".equalsIgnoreCase(op)) {
            appendCode("CMAQ");
        }
    }

    public void incVarAlloc(int positionOfAllocCode) {
        if (finished) {
            throw new IllegalArgumentException("Voce nao pode modificar " +
                    "um codigo concluido");
        }
        String code = mCode.remove(positionOfAllocCode);
        code = code.trim();
        String[] codeParts = code.split(" ");

        if (!"ALLOC".equalsIgnoreCase((codeParts[0]))) {
            throw new IllegalArgumentException("Voce nao pode incrementar uma variavel " +
                    "se o codigo nao for ALLOC");
        }

        int finalVars = Integer.valueOf(codeParts[2]) + 1;
        String newCode = codeParts[0] + " " + codeParts[1] + " " + finalVars;
        mCode.add(positionOfAllocCode, newCode);
    }

    public String getCode() {
        if (!finished) {
            throw new IllegalArgumentException("Antes de gerar o codigo final, voce deve chamar close()");
        }
        StringBuilder sb = new StringBuilder();
        for (String line : mCode) {
            sb.append(line);
        }

        return sb.toString();
    }

    public synchronized void close() {
        finished = true;
    }
}
