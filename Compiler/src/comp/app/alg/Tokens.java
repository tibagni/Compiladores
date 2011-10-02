package comp.app.alg;

import java.util.concurrent.ArrayBlockingQueue;

import comp.app.Token;


public class Tokens {
    private static Tokens sInstance;
    private ArrayBlockingQueue<Token> mTokenBuffer;
    private boolean mIsLexicalFinished;

    public synchronized static Tokens getInstance() {
        if (sInstance == null) {
            sInstance = new Tokens();
        }
        return sInstance;
    }

    private Tokens() {
        mTokenBuffer = new ArrayBlockingQueue<Token>(50);
        mIsLexicalFinished = false;
    }

    /**
     * Adiciona token no buffer.
     * Pode ser que o metodo tenha que esperar ate que o buffer esteja livre
     * 
     * @param t Token a ser inserido no buffer
     */
    public boolean insertTokenInBuffer(Token t) {
        boolean inserted = false;
        int tries = 0;

        inserted = mTokenBuffer.offer(t);     
        // Se o token nao foi inserido espera ate que o buffer esteja vazio
        // E tenta novamente (no maximo 3 vezes)
        if (!inserted) {
            while (!inserted && tries < 3) {
                try { Thread.sleep(10); } catch (InterruptedException e) { /* Nao e para acontecer */ } 
                inserted = mTokenBuffer.offer(t);
            }
        }
        return inserted;
    }

    public Token getTokenFromBuffer() {
        try {
            return mTokenBuffer.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public synchronized void setLexicalFinished() {
        if (!checkClassConsistency(Lexical.class)) {
            throw new IllegalArgumentException("Somente o analisador lexical pode" +
            		" marcar como finalizado");
        }
        mIsLexicalFinished = true;
    }

    public synchronized boolean isLexicalFinishedWithouError() {
        return mIsLexicalFinished && (mTokenBuffer.remainingCapacity() == 0);
    }

    private boolean checkClassConsistency(Class<?> expectedContext) {
        StackTraceElement[] st = Thread.getAllStackTraces().get(Thread.currentThread());
        StackTraceElement element = st[4];
        if (element.getClassName().equals(expectedContext.getName())) {
            return true;
        }
        return false;
    }
}
