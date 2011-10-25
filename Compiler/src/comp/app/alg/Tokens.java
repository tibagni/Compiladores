package comp.app.alg;

import java.util.concurrent.ArrayBlockingQueue;

import comp.app.Token;


public class Tokens {
    private static Tokens sInstance;
    private ArrayBlockingQueue<Token> mTokenBuffer;
    private volatile boolean mIsLexicalFinished;
    private volatile boolean mIsWaitingNextToken = false;
    private Thread mConsumerThread;

    public void setConsumerThread(Thread t) {
        mConsumerThread = t;
    }

    public synchronized static Tokens getInstance() {
        if (sInstance == null) {
            sInstance = new Tokens();
        }
        return sInstance;
    }

    private Tokens() {
        mTokenBuffer = new ArrayBlockingQueue<Token>(800); //FIXME mudar para LinkedListBlockingQueue
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
                tries++;
            }
        }
        return inserted;
    }

    public Token getTokenFromBuffer() {
        try {
            mIsWaitingNextToken = true;
            return mTokenBuffer.take();
        } catch (InterruptedException e) {
            return null;
        } finally {
            mIsWaitingNextToken = false;
        }
    }

    public synchronized void setLexicalFinished() {
        if (!checkClassPermission(Lexical.class)) {
            throw new RuntimeException("Somente o analisador lexical pode"
                    + " marcar como finalizado");
        }
        // Interrompe a thread de compilacao caso ela esteja esperando por um token
        // e nao ha mais tokens a serem consumidos
        if (mConsumerThread != null && mTokenBuffer.size() == 0 &&
                mIsWaitingNextToken) {
            mConsumerThread.interrupt();
        }
        mIsLexicalFinished = true;
    }

    public synchronized boolean isLexicalFinishedWithouError() {
        return mIsLexicalFinished && (mTokenBuffer.size() == 0);
    }

    private boolean checkClassPermission(Class<?> expectedClass) {
        StackTraceElement[] st = Thread.getAllStackTraces().get(Thread.currentThread());
        StackTraceElement element = st[4];
        if (element.getClassName().equals(expectedClass.getName())) {
            return true;
        }
        return false;
    }
}
