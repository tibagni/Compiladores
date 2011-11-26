package comp.app.error;

public class CompilerError {
    public static final int NONE_ERROR                      = 0;
    public static final int UNKNOWN_ERROR                   = -1;
    public static final int NOT_INITIALIZED                 = -2;

    // LEXICAL
    public static final int INVALID_CHAR_ERROR              = 1;
    public static final int INVALID_SYMBOL_ERROR            = 2;
    public static final int INVALID_COMMENT_ERROR           = 3;

    // SINTATICO
    public static final int INVALID_PROGRAM_START           = 4;  // Se o programa nao comeca com a palavra 'programa'
    public static final int INVALID_PROGRAM_NAME            = 5;
    public static final int ILLEGAL_END_EXPRESSION          = 6;  // Faltando ';'
    public static final int ILLEGAL_END_PROGRAM             = 7;
    public static final int UNKNOWN_TYPE                    = 8;
    public static final int ILLEGAL_VAR_DECLARATION         = 9;  // Nome da variavel invalido
    public static final int MALFORMED_VAR_DECLARATION       = 10;
    public static final int ILLEGAL_VAR_TYPE_DECLARATION    = 11; // ':' no lugar errado
    public static final int ILLEGAL_PROC_FUNC_DECLARATION   = 12; // identificador esperado
    public static final int UNKNOWN_RETURN_TYPE             = 13; //
    public static final int ILLEGAL_RETURN_TYPE_DECLARATION = 14; //
    public static final int ILLEGAL_CMD_BLOCK_DECLARATION   = 15; // faltando 'inicio'
    public static final int OPEN_PARENTHESIS_EXPECTED       = 16; // faltando '('
    public static final int MISSING_CLOSE_PARENTHESIS       = 17; // faltando ')'
    public static final int INVALID_PROC_FUNC_NAME          = 18; //
    public static final int MALFORMED_IF_CONSTRUCTION       = 19; // faltando 'entao'
    public static final int MALFORMED_WHILE_CONSTRUCTION    = 20; // faltando 'faca'
    public static final int WRONG_READ_WRITE_ARGUMENT       = 21; // argumento nao eh identificador
    public static final int MALFORMED_EXPRESSION            = 22; // Expressao incorreta
    public static final int UNEXPECTED_TOKEN                = 23;

    // SEMANTICO
    public static final int DUPLICATED_VAR                  = 24;
    public static final int DUPLICATED_SUB_ROUTINE          = 25;
    public static final int IDENTIFIER_NOT_FOUND            = 26;
    public static final int EXPRESSION_BOOLEAN_EXPECTED     = 27;
    public static final int EXPRESSION_INCOMPATIBLE_TYPES   = 28;
    public static final int FUNCTION_WRONG_ATTR             = 29;

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
        return instantiateError(NONE_ERROR, 0, 0);
    }

    private static String buildErrorMessage(int errorCode, int lineNumber, int colNumber) {
        switch (errorCode) {
            case NONE_ERROR:
                return "Compilação finalizada com sucesso!!";

            case INVALID_FILE_ERROR:
                return "Problemas ao ler arquivo!";

            /* Erros identificados na analise lexical *************************************/
            case INVALID_CHAR_ERROR:
                return "Caracter inválido - linha: "
                + lineNumber + " coluna: " + colNumber;

            case INVALID_SYMBOL_ERROR:
                return "Símbolo não existe - linha: "
                + lineNumber + " coluna: " + colNumber;

            case INVALID_COMMENT_ERROR:
                return "Comentário inválido (sem fechamento) - linha: "
                + lineNumber + " coluna: " + colNumber;

            /* Erros identificados na analise sintatica *************************************/
            case INVALID_PROGRAM_START:
                return "O programa deve iniciar com a palavra 'programa'";

            case INVALID_PROGRAM_NAME:
                return "O nome do programa é inválido!";

            case ILLEGAL_END_EXPRESSION:
                return "Expressão finalizada incorretamente - linha: " +
                    lineNumber + " coluna: " + colNumber;

            case ILLEGAL_END_PROGRAM:
            	return "Programa finalizado incorretamente (Faltando '.')";

            case UNKNOWN_TYPE:
            	return "Tipo de dados desconhecido - linha: " +
        		lineNumber + " coluna: " + colNumber;

            case ILLEGAL_VAR_DECLARATION:
                return "Nome de variável inválido - linha: " +
                lineNumber + " coluna: " + colNumber;

            case MALFORMED_VAR_DECLARATION:
                return "Declaração de variável incorreta - linha: " +
                		lineNumber;

            case ILLEGAL_VAR_TYPE_DECLARATION:
                return "Declaração de variável incorreta (':' inesperado) - linha: " +
                lineNumber + " coluna: " + colNumber;

            case ILLEGAL_PROC_FUNC_DECLARATION:
                return "Nome de procedimento/função inválido - linha: " +
                lineNumber + " coluna: " + colNumber;

            case UNKNOWN_RETURN_TYPE:
                return "Retorno de função desconhecido - linha: " +
                lineNumber + " coluna: " + colNumber;

            case ILLEGAL_RETURN_TYPE_DECLARATION:
                return "Declaração de função incorreta (':' faltando) - linha: " +
                lineNumber + " coluna: " + colNumber;

            case ILLEGAL_CMD_BLOCK_DECLARATION:
                return "Bloco de comando incorreto (verifique se o bloco inicia com " +
                		"'inicio' e termina com 'fim') - linha: " +
                lineNumber + " coluna: " + colNumber;

            case OPEN_PARENTHESIS_EXPECTED:
                return "Faltando ´(´ - linha: " +
                lineNumber + " coluna: " + colNumber;

            case MISSING_CLOSE_PARENTHESIS:
                return "Faltando ´)´ - linha: " +
                lineNumber + " coluna: " + colNumber;

            case INVALID_PROC_FUNC_NAME:
                return "Procedimento/Função inválido ou inexistente - linha: " +
                lineNumber + " coluna: " + colNumber;

            case MALFORMED_IF_CONSTRUCTION:
                return "Erro na construção do comando se ('entao' faltando) - linha: " +
                lineNumber + " coluna: " + colNumber;

            case MALFORMED_WHILE_CONSTRUCTION:
                return "Erro na construção do comando enquanto ('faca' faltando) - linha: " +
                lineNumber + " coluna: " + colNumber;

            case WRONG_READ_WRITE_ARGUMENT:
                return "Argumento inválido ao executar leia/escreva - linha: " +
                lineNumber + " coluna: " + colNumber;

            case MALFORMED_EXPRESSION:
                return "Expressão mal formada - linha: " +
                lineNumber;

            case UNEXPECTED_TOKEN:
                return "Token inesperado - linha: " +
                lineNumber;

            /* Erros da analise semantica *************************************/

            case DUPLICATED_VAR:
                return "Variavel duplicada! - linha: " +
                		lineNumber;

            case DUPLICATED_SUB_ROUTINE:
                return "Procedimento ou funcao duplicado(a)! - linha: " +
                        lineNumber;

            case IDENTIFIER_NOT_FOUND:
                return "Identificador nao declarado - linha: " +
                		lineNumber;

            case EXPRESSION_BOOLEAN_EXPECTED:
                return "Boolean esperado mas nao encontrado - linha: " +
                        lineNumber;

            case EXPRESSION_INCOMPATIBLE_TYPES:
                return "Tipos incompatíveis na expressão - linha: " +
                		lineNumber;

            case FUNCTION_WRONG_ATTR:
                return "Nao é permitido atribuir um valor a uma funcao - linha: " +
                		lineNumber;

            /* Erro desconhecido **********************************************/
            case UNKNOWN_ERROR:
                return "Erro desconhecido - linha: "
                + lineNumber + " cluna: " + colNumber;
        }
        return "";
    }
}
