package comp.app;

import java.io.File;
import java.io.IOException;

import comp.app.alg.Lexical;
import comp.app.error.CompilerError;
import comp.app.log.C_Log;

public class Compiler {
    private File mSourceFile;
    
    private Object lex;
    private CompilerError mLexicalOutput = null;

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
        // TODO inicia as threds para a compilação
        
        // Agora a thread principal espera a compilacao
        while (mLexicalOutput == null) {
            try {
                lex.wait();
            } catch (InterruptedException e) {
                C_Log.error("InterruptedException", e);
            }
        }
        
        // Checa erros por prioridades
        /*
         * 1 - Erro lexico
         * 2 - Erro na segunda thread
         */
        if (getLexicalOutput().getErrorCode() != CompilerError.NONE_ERROR_CODE) {
            // TODO informa erro ao usuario e a outra thread para que sua execucao seja cnacelada
        } else {
            // Espera a segunda thread terminar para verificar o status da compilacao
            // TODO wait...
            
        }
    }

    private void setLexicalOutput(CompilerError error) {
        synchronized(mLexicalOutput) {
            mLexicalOutput = error;
        }
    }

    private CompilerError getLexicalOutput() {
        synchronized(mLexicalOutput) {
            return mLexicalOutput;
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
                setLexicalOutput(CompilerError.instantiateError(CompilerError.INVALID_FILE_ERROR, 0, 0, null));
            } finally {
                // Apenas a main Thread esta esperando este objeto
                lex.notify();
            }
        }
    }

    private class CompilingThread implements Runnable {
        @Override
        public void run() {            
        }        
    }
}
