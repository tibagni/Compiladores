package comp.app.alg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import comp.app.Symbols;
import comp.app.Token;
import comp.app.error.CompilerError;
import comp.app.log.C_Log;


public class Lexical extends Algorithm {
    private static final int EOF             = 65535; // FIXME arrumar fim de arquivo
    private static final int CARRIEGE_RETURN = 13;
    private static final int LINE_FEED       = 10;
    private static final int TAB	         = 9;

    private int              lineNumber      = 1;
    private int              colNumber       = 1;


    private Tokens mTokenList      = Tokens.getInstance();

    public CompilerError execute(File file) throws IOException {
        BufferedReader reader;

        CompilerError error = CompilerError.NONE();

        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            C_Log.error("Arquivo nao encontrado!!", e);
            return CompilerError.instantiateError(CompilerError.INVALID_FILE_ERROR,
                    0, 0);
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

                        	// Mesmo dentro dos comentarios ainda devemos pular linhas
                        	if (nextChar == LINE_FEED) {
                        	    lineNumber++;
                        	    colNumber = 0;
                        	}
                        }
                        if (nextChar != '}') {
                            // Erro (Comentario nao fechado)
                            error = CompilerError.instantiateError(CompilerError.INVALID_COMMENT_ERROR_CODE,
                                    commentLine, commentCol);
                            break;
                        }

                        // Le o ultimo caractere do comentario ('}') e ignora
                        nextChar = readNextChar(reader);

                    } else if (nextChar == LINE_FEED) {
                        lineNumber++;
                        colNumber = 0;
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
                       //Concatena com todos os proximos digitos
                        while (isNumber(nextChar)) {
                            num.append(nextChar);
                            nextChar = readNextChar(reader);
                        }
                        // Cria o token com o numero formado
                        token = new Token(num.toString(), Symbols.SNUMERO, lineNumber, colNumber);

                    } else if (isLetter(nextChar)) { // Identificador e palavra
                                                     // reservada
                        StringBuilder id = new StringBuilder();
                        id.append(nextChar);
                        nextChar = readNextChar(reader);
                        // Concatena com letras, digitos e/ou "_"
                        while (isLetter(nextChar) || isNumber(nextChar)
                                || nextChar == '_') {
                            id.append(nextChar);
                            nextChar = readNextChar(reader);
                        }
                        // Cria o token primeiramente com simbolo indefinido
                        token = new Token(id.toString(), Symbols.SINDEFINIDO, lineNumber, colNumber);

                        // Agora verifica qual e o simbolo deste token (identificador ou palavra reservada)
                        Integer symbol = Symbols.KEY_WORD.get(id.toString());
                        if (symbol != null) {
                            token.setSimbolo(symbol.intValue());
                        } else {
                            token.setSimbolo(Symbols.SIDENTIFICADOR);
                        }

                    } else if (nextChar == ':') {
                        StringBuilder builder = new StringBuilder();
                        builder.append(nextChar);
                        nextChar = readNextChar(reader);
                        if (nextChar == '=') {
                            builder.append(nextChar);
                            token = new Token(builder.toString(),
                                    Symbols.SATRIBUICAO, lineNumber, colNumber);
                            nextChar = readNextChar(reader);
                        } else {
                            token = new Token(builder.toString(),
                                    Symbols.SDOISPONTOS, lineNumber, colNumber);
                        }

                    } else if (isArithmetic(nextChar)) {
                        // Simplesmente cria o token apropriado
                        // para depois guardar na lista
                        if (nextChar == '+') {
                            token = new Token("" + nextChar, Symbols.SMAIS, lineNumber, colNumber);
                        } else if (nextChar == '-') {
                            token = new Token("" + nextChar, Symbols.SMENOS, lineNumber, colNumber);
                        } else {
                            token = new Token("" + nextChar, Symbols.SMULT, lineNumber, colNumber);
                        }
                        nextChar = readNextChar(reader);

                    } else if (isRelational(nextChar)) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(nextChar);
                        char prevChar = nextChar;
                        nextChar = readNextChar(reader);
                        if (prevChar == '=') {
                            token = new Token(builder.toString(), Symbols.SIG, lineNumber, colNumber);
                        } else if (prevChar == '>') {
                            if (nextChar == '=') {
                                builder.append(nextChar);
                                token = new Token(builder.toString(),
                                        Symbols.SMAIORIG, lineNumber, colNumber);
                                nextChar = readNextChar(reader);
                            } else {
                                token = new Token(builder.toString(),
                                        Symbols.SMAIOR, lineNumber, colNumber);
                            }
                        } else if (prevChar == '<') {
                            if (nextChar == '=') {
                                builder.append(nextChar);
                                token = new Token(builder.toString(),
                                        Symbols.SMENORIG, lineNumber, colNumber);
                                nextChar = readNextChar(reader);
                            } else {
                                token = new Token(builder.toString(),
                                        Symbols.SMENOR, lineNumber, colNumber);
                            }
                        } else { // prevChar = '!'
                            if (nextChar == '=') {
                                builder.append(nextChar);
                                token = new Token(builder.toString(),
                                        Symbols.SDIF, lineNumber, colNumber);
                                nextChar = readNextChar(reader);
                            } else {
                                error = CompilerError.instantiateError(CompilerError.INVALID_SYMBOL_ERROR_CODE,
                                        lineNumber, (colNumber-1));
                                break;
                            }
                        }

                    } else if (isPunctuation(nextChar)) {
                        // Simplesmente cria o token apropriado
                        // para depois guardar na lista
                        if (nextChar == ';') {
                            token = new Token("" + nextChar, Symbols.SPONTO_VIRGULA, lineNumber, colNumber);
                        } else if (nextChar == ',') {
                            token = new Token("" + nextChar, Symbols.SVIRGULA, lineNumber, colNumber);
                        } else if (nextChar == '(') {
                            token = new Token("" + nextChar, Symbols.SABRE_PARENTESES, lineNumber, colNumber);
                        } else if (nextChar == ')') {
                            token = new Token("" + nextChar, Symbols.SFECHA_PARENTESES, lineNumber, colNumber);
                        } else {
                            token = new Token("" + nextChar, Symbols.SPONTO, lineNumber, colNumber);
                        }
                        nextChar = readNextChar(reader);

                    } else {
                        error = CompilerError.instantiateError(CompilerError.INVALID_CHAR_ERROR_CODE,
                                lineNumber, (colNumber-1));
                        break;
                    }

                }

                // Insere Token na lista
                if (token != null) {
                    boolean ret = mTokenList.insertTokenInBuffer(token);

                    if(C_Log.ENABLED) C_Log.logToken(token);
                    if (C_Log.ENABLED) {
                        if (!ret) {
                            C_Log.message("Token falhou ao ser inserido!: " + token.toString());
                        }
                    }
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
