package comp.app;

import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import comp.app.alg.Lexical;
import comp.app.alg.Syntactic;
import comp.app.alg.Tokens;
import comp.app.error.CompilerError;
import comp.app.log.C_Log;

public class Compiler extends Thread {
    private File mSourceFile;

    private Object lex = new Object();
    private CompilerError mLexicalOutput = CompilerError.instantiateError(CompilerError.NOT_INITIALIZED, 0, 0);
    private Object mLexicalOutputLock = new Object();

    private Object compilation = new Object();
    private CompilerError mOutput = CompilerError.instantiateError(CompilerError.NOT_INITIALIZED, 0, 0);
    private Object mOutputLock = new Object();

    private Thread mLexicalThread;
    private Thread mCompilingThread;

    private UIListener mListener;

    public Compiler(File sourceFile, UIListener listener) {
    	mSourceFile = sourceFile;
    	mListener = listener;
    	setName("CompilationThread");
    }

    @Override
    public void run() {
        compile();
    }

    private void compile() {
        // Inicia as threds para a compilação
        mLexicalThread = new Thread(new LexicalThread(mSourceFile));
        mCompilingThread = new Thread(new CompilingThread());

        mLexicalThread.setName("LexicalThread");
        mCompilingThread.setName("SyntacticSemanticThread");

        Tokens.getInstance().setConsumerThread(mCompilingThread);

        mLexicalThread.start();
        mCompilingThread.start();

        // Agora a thread principal espera a compilacao
        synchronized (lex) {
            while (mLexicalOutput.getErrorCode() == CompilerError.NOT_INITIALIZED) {
                try {
                    lex.wait();
                } catch (InterruptedException e) {
                    C_Log.error("InterruptedException", e);
                }
            }
        }

        // Checa erros por prioridades
        /*
         * 1 - Erro lexico
         * 2 - Erro na segunda thread
         */
        if (getLexicalOutput().getErrorCode() != CompilerError.NONE_ERROR) {
            mCompilingThread.interrupt();
            notifyUIListener(getLexicalOutput());

        } else {
            // Espera a segunda thread terminar para verificar o status da compilacao
            synchronized (compilation) {
                while (mOutput.getErrorCode() == CompilerError.NOT_INITIALIZED) {
                    try {
                        compilation.wait();
                    } catch (InterruptedException e) {
                        C_Log.error("InterruptedException", e);
                    }
                }
            }

            // Checa erros do restante da compilacao (Sintatico e semantico)
            if (getOutput().getErrorCode() != CompilerError.NONE_ERROR) {
                notifyUIListener(getOutput());
            } else {
                notifyUIListener(CompilerError.instantiateError(CompilerError.NONE_ERROR, 0, 0));
            }
        }
    }

    private void setLexicalOutput(CompilerError error) {
        synchronized(mLexicalOutputLock) {
            mLexicalOutput = error;
        }
    }

    private CompilerError getLexicalOutput() {
        synchronized(mLexicalOutputLock) {
            return mLexicalOutput;
        }
    }

    private void setOutput(CompilerError error) {
        synchronized(mOutputLock) {
            mOutput = error;
        }
    }

    private CompilerError getOutput() {
        synchronized(mOutputLock) {
            return mOutput;
        }
    }

    private void notifyUIListener(final CompilerError result) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onCompilationFinished(result);
                }
            }
        });
    }

    /*
     * Threads que serao usadas para a compilacao (alem da principal)
     */

    private class LexicalThread implements Runnable {
        private File mSourceFile;

        public LexicalThread(File sourceFile) {
            mSourceFile = sourceFile;
        }

        @Override
        public void run() {
            Lexical lexical = new Lexical();
            CompilerError error = null;
            try {
                error = lexical.execute(mSourceFile);
                // Avisa a thread principal que a execucao do lexico terminou
                // e informa se houve erros
                setLexicalOutput(error);
            } catch (IOException e) {
                C_Log.error("Erro no arquivo!", e);
                setLexicalOutput(CompilerError.instantiateError(CompilerError.INVALID_FILE_ERROR, 0, 0));
            } finally {
                // Apenas a main Thread esta esperando este objeto
                synchronized (lex) {
                    lex.notify();
                }
            }
        }
    }

    private class CompilingThread implements Runnable {
        @Override
        public void run() {
            Syntactic syntactic = new Syntactic(mLexicalThread);
            CompilerError error = null;
            error = syntactic.execute();
            setOutput(error);

            // Apenas a main Thread esta esperando este objeto
            synchronized (compilation) {
                compilation.notify();
            }
        }
    }

    public interface UIListener {
        void onCompilationFinished(CompilerError result);
    }
}
