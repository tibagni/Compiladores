package comp.app.alg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import comp.app.Simbolos;
import comp.app.Token;
import comp.app.error.CompilerError;
import comp.app.log.C_Log;


public class Lexical extends Algorithm {
    private static final int EOF             = 65535; // FIXME arrumar fim de arquivo
    private static final int CARRIEGE_RETURN = 13;
    private static final int LINE_FEED       = 10;
    private static final int TAB	     = 9;

    private int              lineNumber      = 1;
    private int              colNumber       = 0;
    

    private Tokens mTokenList      = Tokens.getInstance();

    public CompilerError execute(File file) throws IOException {
        BufferedReader reader;
        
        CompilerError error = CompilerError.NONE();

        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            C_Log.error("Arquivo nao encontrado!!", e);
            return CompilerError.instantiateError(CompilerError.INVALID_FILE_ERROR, 
                    0, 0, this);
        }

        if (reader != null) {
            // Lê o próximo caractere
            char nextChar = readNextChar(reader);
            while (nextChar != EOF) {
                Token token = null;
                // Consome (ignora) comentarios e espacos
                while (nextChar == '{' || nextChar == ' '
                        || nextChar == CARRIEGE_RETURN || nextChar == LINE_FEED || nextChar == TAB) {
                    if (nextChar == '{') {
                        int commentLine = lineNumber;  // Salva a linha e a coluna onde
                        int commentCol = colNumber;    // o comentário foi iniciado 
                        
                        // O texto a seguir e comentario, vamos ignorar.
                        while (nextChar != '}' && nextChar != EOF) {
                            // Consome caractere dentro do comentario (ignora).
                        	nextChar = readNextChar(reader);
                        }
                        if (nextChar != '}') {
                            // Erro (Comentario nao fechado)
                            error = CompilerError.instantiateError(CompilerError.INVALID_COMMENT_ERROR_CODE, 
                                    commentLine, commentCol, this);
                            break;
                        }

                        // Le o ultimo caractere do comentario ('}') e ignora
                        nextChar = readNextChar(reader);

                    } else if (nextChar == LINE_FEED) { // Lê o caractere line_feed e o ignora
                        lineNumber++;	// Incrementa o contador de linhas
                        colNumber = 0; // Reseta contador de colunas na proxima linha
                        nextChar = readNextChar(reader);
                    }

                    // Ignora todos os espacos em sequencia encontrados, assim como TABs e voltas de linhas.
                    while ((nextChar == ' ' || nextChar == CARRIEGE_RETURN || nextChar == TAB) 
                    		&& nextChar != EOF) {
                    	nextChar = readNextChar(reader);
                    }
                }

                if (nextChar != EOF) {
                    if (isNumber(nextChar)) { // É numero
                        StringBuilder num = new StringBuilder();
                        num.append(nextChar);
                        nextChar = readNextChar(reader);
                        while (isNumber(nextChar)) { //Concatena com todos os proximos digitos
                            num.append(nextChar);
                            nextChar = readNextChar(reader);
                        }
                        // Cria o token com o numero formado
                        token = new Token(num.toString(), Simbolos.SNUMERO);

                    } else if (isLetter(nextChar)) { // Identificador e palavra
                                                     // reservada
                        StringBuilder id = new StringBuilder();
                        id.append(nextChar);
                        nextChar = readNextChar(reader);
                        while (isLetter(nextChar) || isNumber(nextChar)
                                || nextChar == '_') { // Concatena com letras, digitos e/ou "_"
                            id.append(nextChar);
                            nextChar = readNextChar(reader);
                        }
                        // Cria o token primeiramente com simbolo indefinido
                        token = new Token(id.toString(), Simbolos.SINDEFINIDO);
                        
                        // Agora verifica qual e o simbolo deste token
                        try {
                            token.setSimbolo(Simbolos.PALAVRAS_RESERVADAS
                                    .get(id.toString()));
                        } catch (NullPointerException e) {      // Se esta excessao for gerada, quer dizer
                            token.setSimbolo(Simbolos.SIDENTIFICADOR); // que o simbolo é um identificador
                        }

                    } else if (nextChar == ':') {
                        StringBuilder builder = new StringBuilder();
                        builder.append(nextChar);
                        nextChar = readNextChar(reader);
                        if (nextChar == '=') {
                            builder.append(nextChar);
                            token = new Token(builder.toString(),
                                    Simbolos.SATRIBUICAO);
                            nextChar = readNextChar(reader);
                        } else {
                            token = new Token(builder.toString(),
                                    Simbolos.SDOISPONTOS);
                        }

                    } else if (isArithmetic(nextChar)) {
                        // Simplesmente cria o token apropriado
                        // para depois guardar na lista
                        if (nextChar == '+') {
                            token = new Token("" + nextChar, Simbolos.SMAIS);
                        } else if (nextChar == '-') {
                            token = new Token("" + nextChar, Simbolos.SMENOS);
                        } else {
                            token = new Token("" + nextChar, Simbolos.SMULT);
                        }
                        nextChar = readNextChar(reader);

                    } else if (isRelational(nextChar)) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(nextChar);
                        char prevChar = nextChar;
                        nextChar = readNextChar(reader);
                        if (prevChar == '=') {
                            token = new Token(builder.toString(), Simbolos.SIG);
                        } else if (prevChar == '>') {
                            if (nextChar == '=') {
                                builder.append(nextChar);
                                token = new Token(builder.toString(),
                                        Simbolos.SMAIORIG);
                                nextChar = readNextChar(reader);
                            } else {
                                token = new Token(builder.toString(),
                                        Simbolos.SMAIOR);
                            }
                        } else if (prevChar == '<') {
                            if (nextChar == '=') {
                                builder.append(nextChar);
                                token = new Token(builder.toString(),
                                        Simbolos.SMENORIG);
                                nextChar = readNextChar(reader);
                            } else {
                                token = new Token(builder.toString(),
                                        Simbolos.SMENOR);
                            }
                        } else { // prevChar = '!'
                            if (nextChar == '=') {
                                builder.append(nextChar);
                                token = new Token(builder.toString(),
                                        Simbolos.SDIF);
                                nextChar = readNextChar(reader);
                            } else {
                                // Erro (simbolo nao existe)
                                error = CompilerError.instantiateError(CompilerError.INVALID_SYMBOL_ERROR_CODE, 
                                        lineNumber, (colNumber-1), this);
                                break;
                            }
                        }

                    } else if (isPunctuation(nextChar)) {
                        // Simplesmente cria o token apropriado
                        // para depois guardar na lista
                        if (nextChar == ';') {
                            token = new Token("" + nextChar, Simbolos.SPONTO_VIRGULA);
                        } else if (nextChar == ',') {
                            token = new Token("" + nextChar, Simbolos.SVIRGULA);
                        } else if (nextChar == '(') {
                            token = new Token("" + nextChar, Simbolos.SABRE_PARENTESES);
                        } else if (nextChar == ')') {
                            token = new Token("" + nextChar, Simbolos.SFECHA_PARENTESES);
                        } else {
                            token = new Token("" + nextChar, Simbolos.SPONTO);
                        }
                        nextChar = readNextChar(reader);

                    } else {
                        // Erro
                        error = CompilerError.instantiateError(CompilerError.INVALID_CHAR_ERROR_CODE, 
                                lineNumber, (colNumber-1), this);
                        break;
                    }

                }

                // Insere Token na lista
                if (token != null) {
                    mTokenList.insertTokenInBuffer(token);
                    //Log para debugar.
                    if(C_Log.ENABLED) C_Log.logToken(token);
                }
            }
            reader.close();
        }

        // Indica que o analisador lexico encerrou
        mTokenList.setLexicalFinished();
        return error;
    }

    /** Lê o próximo caractere do fonte e 
     * incrementa a coluna (posicionamento da leitura) */
    private char readNextChar(BufferedReader n) throws IOException {
    	colNumber++;
    	return (char) n.read();
    }
    
    /** Verifica se o caractere é um digito */
    private boolean isNumber(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        return false;
    }
 
    /** Verifica se o caractere é uma letra */
    private boolean isLetter(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            return true;
        }
        return false;
    }

    /** Verifica se o caractere é um simbolo aritmetico */
    private boolean isArithmetic(char c) {
        if (c == '+' || c == '-' || c == '*') {
            return true;
        }
        return false;
    }

    /** Verifica se o caractere é um simbolo relacional */
    private boolean isRelational(char c) {
        if (c == '>' || c == '<' || c == '=' || c == '!') {
            return true;
        }
        return false;
    }

    /** Verifica se o caractere é um simbolo de pontuação */
    private boolean isPunctuation(char c) {
        if (c == ';' || c == ',' || c == '(' || c == ')' || c == '.') {
            return true;
        }
        return false;
    }
}
