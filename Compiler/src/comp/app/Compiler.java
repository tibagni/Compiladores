package comp.app;

import java.io.File;
import java.io.IOException;

import comp.app.alg.Lexical;
import comp.app.alg.Syntactic;
import comp.app.alg.Tokens;
import comp.app.error.CompilerError;
import comp.app.log.C_Log;

public class Compiler {
    private File mSourceFile;

    private Object lex = new Object();
    private CompilerError mLexicalOutput = CompilerError.instantiateError(CompilerError.NOT_INITIALIZED, 0, 0);
    private Object mLexicalOutputLock = new Object();

    private Object compilation = new Object();
    private CompilerError mOutput = CompilerError.instantiateError(CompilerError.NOT_INITIALIZED, 0, 0);
    private Object mOutputLock = new Object();

    private Thread mLexicalThread;
    private Thread mCompilingThread;

    /**
     * @param args
     */
    public static void main(String[] args) {
        String fileName = args[0];
        File f = new File(fileName);

        // Limpa todos os arquivos de log
        C_Log.clearLogFiles();

        if (f != null && f.exists()) {
            new Compiler(f).compile();
        }
    }

    public Compiler(File sourceFile) {
        mSourceFile = sourceFile;
    }

    public void compile() {
        // Inicia as threds para a compilação
        mLexicalThread = new Thread(new LexicalThread(mSourceFile));
        mCompilingThread = new Thread(new CompilingThread());

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
            // TODO informa erro ao usuario e a outra thread para que sua execucao seja cnacelada
            mCompilingThread.interrupt();
            System.out.println(getLexicalOutput().getErrorMessage());
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
                // TODO informa erro
                System.out.println(getOutput().getErrorMessage());
            } else {
                // TODO compilacao concluida com sucesso
                System.out.println("Sucesso!!");
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
}
