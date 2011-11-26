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
