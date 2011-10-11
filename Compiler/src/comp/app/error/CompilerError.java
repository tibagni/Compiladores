package comp.app.error;

import comp.app.alg.Algorithm;
import comp.app.alg.Lexical;
import comp.app.alg.Syntactic;


public class CompilerError {
    public static final int NONE_ERROR_CODE            = 0;
    public static final int UNKNOWN_ERROR_CODE         = -1;
    public static final int NOT_INITIALIZED            = -2;

    // LEXICAL
    public static final int INVALID_CHAR_ERROR_CODE    = 1;
    public static final int INVALID_SYMBOL_ERROR_CODE  = 2;
    public static final int INVALID_COMMENT_ERROR_CODE = 3;
    
    // SINTATICO
    public static final int INVALID_PROGRAM_START      = 4; // Se o programa nao comeca com a palavra 'programa'
    public static final int INVALID_PROGRAM_NAME       = 5;
    public static final int ILLEGAL_END_EXPRESSION     = 6;
    public static final int ILLEGAL_END_PROGRAM        = 7;
    public static final int ILLEGAL_DECLARATION    	   = 8;
    public static final int UNKNOWN_TYPE               = 9;

    // SEMANTICO
    public static final int DUPLICATED_VAR             = 10;
    
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
    public static CompilerError instantiateError(int errorCode, int lineNumber, int colNumber,
            Algorithm context) {
     
        // Protecao para instanciar apenas erros existentes e consistentes
        // com quem requisitou a instanciacao evitando instancias inconsistentes
        if (!checkConsistency(errorCode, context)) {
            throw new IllegalStateException("Tipo de erro: " + errorCode +
                    " não consiste com o algoritmo que requisitou a instanciação" +
                    " ou não existe!!!");
        }

        return new CompilerError(errorCode, lineNumber, colNumber, 
                buildErrorMessage(errorCode, lineNumber, colNumber));
    }

    private static boolean checkConsistency(int errorCode, Algorithm context) {
        switch (errorCode) {
            // Erros que podem ocorrer em qualquer parte 
            // da compilacao
            case NONE_ERROR_CODE:
            case UNKNOWN_ERROR_CODE:
            case INVALID_FILE_ERROR:
            case NOT_INITIALIZED:
                return true;
                
            // Erros que podem ocorrer somente no algoritmo
            // de analise lexical
            case INVALID_CHAR_ERROR_CODE:
            case INVALID_COMMENT_ERROR_CODE:
            case INVALID_SYMBOL_ERROR_CODE:
                if (context instanceof Lexical) return true;
                break;
           
            // Erros que podem ocorrer somente na analise sintatica
            case INVALID_PROGRAM_START:
            case INVALID_PROGRAM_NAME:
            case ILLEGAL_END_EXPRESSION:
            case ILLEGAL_END_PROGRAM:
            case ILLEGAL_DECLARATION:
            case UNKNOWN_TYPE:
                if (context instanceof Syntactic) return true;
                break;
        }
        return false;
    }
    
    /**
     * @return Instancia sem erro
     */
    public static CompilerError NONE() {
        // Erro NONE nao precisa de contexo (nao sera avaliado)
        return instantiateError(NONE_ERROR_CODE, 0, 0, null);
    }

    private static String buildErrorMessage(int errorCode, int lineNumber, int colNumber) {
        switch (errorCode) {
            case NONE_ERROR_CODE:
                return "Sem erros";
            
            case INVALID_FILE_ERROR:
                return "Problemas ao ler arquivo!";
                
            case INVALID_CHAR_ERROR_CODE:
                return "Caracter inválido - linha: " 
                + lineNumber + " coluna: " + colNumber;
                
            case INVALID_SYMBOL_ERROR_CODE:
                return "Símbolo não existe - linha: " 
                + lineNumber + " coluna: " + colNumber;
                
            case INVALID_COMMENT_ERROR_CODE:
                return "Comentário inválido (sem fechamento) - linha: " 
                + lineNumber + " coluna: " + colNumber;

            case INVALID_PROGRAM_START:
                return "O programa deve iniciar com a palavra 'programa'";

            case INVALID_PROGRAM_NAME:
                return "O nome do programa é inválido!";

            case ILLEGAL_END_EXPRESSION:
                return "Expressão finalizada incorretamente - linha: " +
                    lineNumber + " coluna: " + colNumber;

            case ILLEGAL_END_PROGRAM:
            	return "Programa finalizado incorretamente (Faltando '.')";

            case ILLEGAL_DECLARATION:
            	return "Declaraçao incorreta - linha: " +
            		lineNumber + " coluna: " + colNumber;

            case UNKNOWN_TYPE:
            	return "Tipo de dados desconhecido - linha: " +
        		lineNumber + " coluna: " + colNumber;
                
            case UNKNOWN_ERROR_CODE:
                return "Erro desconhecido - linha: " 
                + lineNumber + " cluna: " + colNumber;
        }
        return "";
    }
}
