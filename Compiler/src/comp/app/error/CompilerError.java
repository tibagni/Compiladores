package comp.app.error;

import comp.app.alg.Algorithm;
import comp.app.alg.Lexical;


public class CompilerError {
    public static final int NONE_ERROR_CODE            = 0;
    public static final int UNKNOWN_ERROR_CODE         = -1;

    // LEXICAL
    public static final int INVALID_CHAR_ERROR_CODE    = 1;
    public static final int INVALID_SYMBOL_ERROR_CODE  = 2;
    public static final int INVALID_COMMENT_ERROR_CODE = 3;
    
    // Outros
    public static final int INVALID_FILE_ERROR         = 100;

    private int mLineNumber;
    private int mColNumber;
    private int mErrorCode;
    private String mErrorMessage;

    private CompilerError(int errorCode, int lineNumber, int colNumber, String msg) {
        mLineNumber = lineNumber;
        mColNumber = colNumber;
        mErrorCode = errorCode;
        mErrorMessage = msg;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public int getLineNumber() {
        return mLineNumber;
    }

    public int getColNumber() {
        return mColNumber;
    }
    
    public String getErrorMessage() {
        return mErrorMessage;
    }

    /**
     * Instancia um erro de acordo com o codigo de erro.
     * 
     * 
     * @param errorCode
     * @param lineNumber
     * @param colNumber
     * @return Instancia do erro
     */
    public static CompilerError InstantiateError(int errorCode, int lineNumber, int colNumber,
            Algorithm context) {
     
        // Protecao para instanciar apenas erros existentes e consistentes
        // com quem requisitou a instanciacao evitando instancias inconsistentes
        if (!checkConsistency(errorCode, context)) {
            throw new IllegalStateException("Tipo de erro: " + errorCode +
                    " não consiste com o algoritmo que requisitou a instanciação" +
                    " ou não existe!!!");
        }

        return new CompilerError(errorCode, 0, 0, buildErrorMessage(errorCode, 0, 0));
    }

    private static boolean checkConsistency(int errorCode, Algorithm context) {
        switch (errorCode) {
            // Erros que podem ocorrer em qualquer parte 
            // da compilacao
            case NONE_ERROR_CODE:
            case UNKNOWN_ERROR_CODE:
            case INVALID_FILE_ERROR:
                return true;
                
            // Erros que podem ocorrer somente no algoritmo
            // de analise lexical
            case INVALID_CHAR_ERROR_CODE:
            case INVALID_COMMENT_ERROR_CODE:
            case INVALID_SYMBOL_ERROR_CODE:
                if (context instanceof Lexical) return true;
                break;
        }
        return false;
    }
    
    /**
     * @return Instancia sem erro
     */
    public static CompilerError NONE() {
        // Erro NONE nao precisa de contexo (nao sera avaliado)
        return InstantiateError(NONE_ERROR_CODE, 0, 0, null);
    }

    private static String buildErrorMessage(int errorCode, int lineNumber, int colNumber) {
        switch (errorCode) {
            case NONE_ERROR_CODE:
                return "Sem erros";
            
            case INVALID_FILE_ERROR:
                return "Problemas ao ler arquivo!";
                
            case INVALID_CHAR_ERROR_CODE:
                return "Caracter inválido - linha: " 
                + lineNumber + " cluna: " + colNumber;
                
            case INVALID_SYMBOL_ERROR_CODE:
                return "Símbolo não existe - linha: " 
                + lineNumber + " cluna: " + colNumber;
                
            case INVALID_COMMENT_ERROR_CODE:
                return "Comentário inválido (sem fechamento) - linha: " 
                + lineNumber + " cluna: " + colNumber;
                
            case UNKNOWN_ERROR_CODE:
                return "Erro desconhecido - linha: " 
                + lineNumber + " cluna: " + colNumber;
        }
        return null;
    }
}
