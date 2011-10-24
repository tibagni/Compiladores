package comp.app.error;

public class CompilerError {
    public static final int NONE_ERROR_CODE                 = 0;
    public static final int UNKNOWN_ERROR_CODE              = -1;
    public static final int NOT_INITIALIZED                 = -2;

    // LEXICAL
    public static final int INVALID_CHAR_ERROR_CODE         = 1;
    public static final int INVALID_SYMBOL_ERROR_CODE       = 2;
    public static final int INVALID_COMMENT_ERROR_CODE      = 3;

    // SINTATICO
    public static final int INVALID_PROGRAM_START           = 4;  // Se o programa nao comeca com a palavra 'programa'
    public static final int INVALID_PROGRAM_NAME            = 5;
    public static final int ILLEGAL_END_EXPRESSION          = 6;  // Faltando ';'
    public static final int ILLEGAL_END_PROGRAM             = 7;
    public static final int UNKNOWN_TYPE                    = 8;
    public static final int ILLEGAL_VAR_DECLARATION         = 9;
    public static final int ILLEGAL_VAR_TYPE_DECLARATION    = 10; // ':' no lugar errado
    public static final int ILLEGAL_PROC_FUNC_DECLARATION   = 11; // identificador esperado
    public static final int UNKNOWN_RETURN_TYPE             = 12; //
    public static final int ILLEGAL_RETURN_TYPE_DECLARATION = 13; //
    public static final int ILLEGAL_CMD_BLOCK_DECLARATION   = 14; // faltando 'inicio'
    public static final int OPEN_PARENTHESIS_EXPECTED       = 15; // faltando '('
    public static final int MISSING_CLOSE_PARENTHESIS       = 16; // faltando ')'
    public static final int INVALID_PROC_FUNC_NAME          = 17; //
    public static final int MALFORMED_IF_CONSTRUCTION       = 18; // faltando 'entao'
    public static final int MALFORMED_WHILE_CONSTRUCTION    = 19; // faltando 'faca'
    public static final int WRONG_READ_WRITE_ARGUMENT       = 20; // argumento nao eh identificador
    public static final int MALFORMED_EXPRESSION            = 21; // Expressao incorreta

    // SEMANTICO
    public static final int DUPLICATED_VAR                  = 22;

    // Outros
    public static final int INVALID_FILE_ERROR              = 100;

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
    public static CompilerError instantiateError(int errorCode, int lineNumber, int colNumber) {

        return new CompilerError(errorCode, lineNumber, colNumber,
                buildErrorMessage(errorCode, lineNumber, colNumber));
    }

    /**
     * @return Instancia sem erro
     */
    public static CompilerError NONE() {
        return instantiateError(NONE_ERROR_CODE, 0, 0);
    }

    private static String buildErrorMessage(int errorCode, int lineNumber, int colNumber) {
        switch (errorCode) {
            case NONE_ERROR_CODE:
                return "Sem erros";

            case INVALID_FILE_ERROR:
                return "Problemas ao ler arquivo!";

            /* Erros identificados na analise lexical *************************************/
            case INVALID_CHAR_ERROR_CODE:
                return "Caracter inv�lido - linha: "
                + lineNumber + " coluna: " + colNumber;

            case INVALID_SYMBOL_ERROR_CODE:
                return "S�mbolo n�o existe - linha: "
                + lineNumber + " coluna: " + colNumber;

            case INVALID_COMMENT_ERROR_CODE:
                return "Coment�rio inv�lido (sem fechamento) - linha: "
                + lineNumber + " coluna: " + colNumber;

            /* Erros identificados na analise sintatica *************************************/
            case INVALID_PROGRAM_START:
                return "O programa deve iniciar com a palavra 'programa'";

            case INVALID_PROGRAM_NAME:
                return "O nome do programa � inv�lido!";

            case ILLEGAL_END_EXPRESSION:
                return "Express�o finalizada incorretamente - linha: " +
                    lineNumber + " coluna: " + colNumber;

            case ILLEGAL_END_PROGRAM:
            	return "Programa finalizado incorretamente (Faltando '.')";

            case UNKNOWN_TYPE:
            	return "Tipo de dados desconhecido - linha: " +
        		lineNumber + " coluna: " + colNumber;

            case ILLEGAL_VAR_DECLARATION:
                return "Nome de vari�vel inv�lido - linha: " +
                lineNumber + " coluna: " + colNumber;

            case ILLEGAL_VAR_TYPE_DECLARATION:
                return "Declara��o de vari�vel incorreta (':' inesperado) - linha: " +
                lineNumber + " coluna: " + colNumber;

            case ILLEGAL_PROC_FUNC_DECLARATION:
                return "Nome de procedimento/fun��o inv�lido - linha: " +
                lineNumber + " coluna: " + colNumber;

            case UNKNOWN_RETURN_TYPE:
                return "Retorno de fun��o desconhecido - linha: " +
                lineNumber + " coluna: " + colNumber;

            case ILLEGAL_RETURN_TYPE_DECLARATION:
                return "Declara��o de fun��o incorreta (':' faltando) - linha: " +
                lineNumber + " coluna: " + colNumber;

            case ILLEGAL_CMD_BLOCK_DECLARATION:
                return "Bloco de comando incorreto ('inicio' faltando) - linha: " +
                lineNumber + " coluna: " + colNumber;

            case OPEN_PARENTHESIS_EXPECTED:
                return "Faltando �(� - linha: " +
                lineNumber + " coluna: " + colNumber;

            case MISSING_CLOSE_PARENTHESIS:
                return "Faltando �)� - linha: " +
                lineNumber + " coluna: " + colNumber;

            case INVALID_PROC_FUNC_NAME:
                return "Procedimento/Fun��o inv�lido ou inexistente - linha: " +
                lineNumber + " coluna: " + colNumber;

            case MALFORMED_IF_CONSTRUCTION:
                return "Erro na constru��o do comando se ('entao' faltando) - linha: " +
                lineNumber + " coluna: " + colNumber;

            case MALFORMED_WHILE_CONSTRUCTION:
                return "Erro na constru��o do comando enquanto ('faca' faltando) - linha: " +
                lineNumber + " coluna: " + colNumber;

            case WRONG_READ_WRITE_ARGUMENT:
                return "Argumento inv�lido ao executar leia/escreva - linha: " +
                lineNumber + " coluna: " + colNumber;

            case MALFORMED_EXPRESSION:
                return "Express�o mal formada - linha: " +
                lineNumber;

            /* Erros da analise semantica *************************************/


            /* Erro desconhecido **********************************************/
            case UNKNOWN_ERROR_CODE:
                return "Erro desconhecido - linha: "
                + lineNumber + " cluna: " + colNumber;
        }
        return "";
    }
}
