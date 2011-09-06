package comp.app.alg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import comp.app.Simbolos;
import comp.app.Token;

public class Lexical {
    private static final int EOF             = 65535; // FIXME arrumar fim de arquivo
    private static final int CARRIEGE_RETURN = 13;
    private static final int LINE_FEED       = 10;

    private int              lineNumber      = 1;
    private int              colNumber       = 1; // TODO
    

    private ArrayList<Token> mTokenList      = new ArrayList<Token>();

    public void analiseLexica(File file) throws IOException {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        if (reader != null) {
            char nextChar = (char) reader.read();
            while (nextChar != EOF) {
                Token token = null;
                // Consome (ignora) comentarios e espacos
                while (nextChar == '{' || nextChar == ' '
                        || nextChar == CARRIEGE_RETURN || nextChar == LINE_FEED) {
                    if (nextChar == '{') {
                        int commentLine = lineNumber;
                        // O texto a seguir e comentario, vamos ignorar.
                        while (nextChar != '}' && nextChar != EOF) {
                            // Consome caractere dentro do comentario (ignora).
                            nextChar = (char) reader.read();
                        }
                        if (nextChar != '}') {
                            // Erro (Comentario nao fechado)
                            System.out.println("Comentario nao finalizado: "
                                    + commentLine);
                            break;
                        }

                        // Le o ultimo caractere do comentario ('}') e ignora
                        nextChar = (char) reader.read();

                    } else if (nextChar == LINE_FEED) {
                        lineNumber++;
                        colNumber = 1; // Reseta contador de colunas na proxima linha
                        nextChar = (char) reader.read();
                    } else if (nextChar == CARRIEGE_RETURN) {
                        nextChar = (char) reader.read();
                    }

                    // Ignora todos os espacos em sequencia encontrados
                    while (nextChar == ' ' && nextChar != EOF) {
                        nextChar = (char) reader.read();
                    }
                }

                if (nextChar != EOF) {
                    if (isNumber(nextChar)) { // E numero
                        StringBuilder num = new StringBuilder();
                        num.append(nextChar);
                        nextChar = (char) reader.read();
                        while (isNumber(nextChar)) {
                            num.append(nextChar);
                            nextChar = (char) reader.read();
                        }
                        token = new Token(num.toString(), Simbolos.SNUMERO);

                    } else if (isLetter(nextChar)) { // Identificador e palavra
                                                     // reservada
                        StringBuilder id = new StringBuilder();
                        id.append(nextChar);
                        nextChar = (char) reader.read();
                        while (isLetter(nextChar) || isNumber(nextChar)
                                || nextChar == '_') {
                            id.append(nextChar);
                            nextChar = (char) reader.read();
                        }
                        token = new Token(id.toString(), Simbolos.SINDEFINIDO);
                        // Verifica qual e o simbolo deste token

                        try {
                            token.setSimbolo(Simbolos.PALAVRAS_RESERVADAS
                                    .get(id.toString()));
                        } catch (NullPointerException e) {
                            token.setSimbolo(Simbolos.SIDENTIFICADOR);
                        }

                    } else if (nextChar == ':') {
                        StringBuilder builder = new StringBuilder();
                        builder.append(nextChar);
                        nextChar = (char) reader.read();
                        if (nextChar == '=') {
                            builder.append(nextChar);
                            token = new Token(builder.toString(),
                                    Simbolos.SATRIBUICAO);
                            nextChar = (char) reader.read();
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
                        nextChar = (char) reader.read();

                    } else if (isRelational(nextChar)) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(nextChar);
                        char prevChar = nextChar;
                        nextChar = (char) reader.read();
                        if (prevChar == '=') {
                            token = new Token(builder.toString(), Simbolos.SIG);
                        } else if (prevChar == '>') {
                            if (nextChar == '=') {
                                builder.append(nextChar);
                                token = new Token(builder.toString(),
                                        Simbolos.SMAIORIG);
                                nextChar = (char) reader.read();
                            } else {
                                token = new Token(builder.toString(),
                                        Simbolos.SMAIOR);
                            }
                        } else if (prevChar == '<') {
                            if (nextChar == '=') {
                                builder.append(nextChar);
                                token = new Token(builder.toString(),
                                        Simbolos.SMENORIG);
                                nextChar = (char) reader.read();
                            } else {
                                token = new Token(builder.toString(),
                                        Simbolos.SMENOR);
                            }
                        } else { // prevChar = '!'
                            if (nextChar == '=') {
                                builder.append(nextChar);
                                token = new Token(builder.toString(),
                                        Simbolos.SDIF);
                                nextChar = (char) reader.read();
                            } else {
                                // Erro (simbolo nao existe)
                                System.out.println("S�mbolo inv�lido. Linha: "
                                        + lineNumber);
                                break;
                            }
                        }

                    } else if (isPunctuation(nextChar)) {
                        // Simplesmente cria o token apropriado
                        // para depois guardar na lista
                        if (nextChar == ';') {
                            token = new Token("" + nextChar,
                                    Simbolos.SPONTO_VIRGULA);
                        } else if (nextChar == ',') {
                            token = new Token("" + nextChar, Simbolos.SVIRGULA);
                        } else if (nextChar == '(') {
                            token = new Token("" + nextChar,
                                    Simbolos.SABRE_PARENTESES);
                        } else if (nextChar == ')') {
                            token = new Token("" + nextChar,
                                    Simbolos.SFECHA_PARENTESES);
                        } else {
                            token = new Token("" + nextChar, Simbolos.SPONTO);
                        }
                        nextChar = (char) reader.read();

                    } else {
                        // Erro
                        System.out.println("Caractere inv�lido. Linha: "
                                + lineNumber);
                        break;
                    }

                }

                // Insere Token na lista
                if (token != null) {
                    mTokenList.add(token);
                }
            }
            reader.close();

            // TODO remover for (teste)
            for (Token t : mTokenList) {
                System.out.println(t);
            }
        }
    }

    private boolean isNumber(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        return false;
    }

    private boolean isLetter(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            return true;
        }
        return false;
    }

    private boolean isArithmetic(char c) {
        if (c == '+' || c == '-' || c == '*') {
            return true;
        }
        return false;
    }

    private boolean isRelational(char c) {
        if (c == '>' || c == '<' || c == '=' || c == '!') {
            return true;
        }
        return false;
    }

    private boolean isPunctuation(char c) {
        if (c == ';' || c == ',' || c == '(' || c == ')' || c == '.') {
            return true;
        }
        return false;
    }
}
