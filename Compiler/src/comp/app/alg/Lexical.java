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
	private static final int EOF = 65535;
	private static final int CARRIEGE_RETURN = 13;
	private static final int LINE_FEED = 10;

	public int lineNumber = 1;

	private ArrayList<Token> mTokenList = new ArrayList<Token>();

	public void analiseLexica(File file) throws IOException {
		BufferedReader reader;

		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		if (reader != null) {
			char nextChar = (char)reader.read();            
			while (nextChar != EOF) {
				Token token = null;
				// Consome (ignora) comentarios e espacos
				while (nextChar == '{' || nextChar == ' ' || nextChar == CARRIEGE_RETURN 
						|| nextChar == LINE_FEED) {
					if (nextChar == '{') {
						// O texto a seguir e comentario, vamos ignorar.
						while (nextChar != '}' && nextChar != EOF) {
							// Consome caractere dentro do comentario (ignora).
							nextChar = (char)reader.read();
						}
						// Le o ultimo caractere do comentario ('}') e ignora
						nextChar = (char)reader.read();
					}
					else if(nextChar == LINE_FEED) {
						lineNumber++;
						nextChar = (char)reader.read();
					}
					else if(nextChar == CARRIEGE_RETURN) {
						nextChar = (char)reader.read();
					}

					// Ignora todos os espacos em sequencia encontrados
					while (nextChar == ' ' && nextChar != EOF) {
						nextChar = (char)reader.read();
					}
				}

				if (nextChar != EOF) {
					if (isNumber(nextChar)) { // E numero
						StringBuilder num = new StringBuilder();
						num.append(nextChar);
						nextChar = (char)reader.read();
						while (isNumber(nextChar)) {
							num.append(nextChar);
							nextChar = (char)reader.read();
						}
						token = new Token(num.toString(), Simbolos.SNUMERO);

					} else if (isLetter(nextChar)) { // Identificador e palavra reservada
						StringBuilder id = new StringBuilder();
						id.append(nextChar);
						nextChar = (char)reader.read();
						while (isLetter(nextChar) || isNumber(nextChar) || nextChar == '_') {
							id.append(nextChar);
							nextChar = (char)reader.read();                            
						}
						token = new Token(id.toString(), Simbolos.SINDEFINIDO);
						// Verifica qual e o simbolo deste token

						try {
							token.setSimbolo(Simbolos.PALAVRAS_RESERVADAS.get(id.toString()));
						}catch(NullPointerException e){
							token.setSimbolo(Simbolos.SIDENTIFICADOR);
						}

					} else if (nextChar == ':') {
						StringBuilder builder = new StringBuilder();
						builder.append(nextChar);
						nextChar = (char)reader.read();
						if (nextChar == '=') {
							builder.append(nextChar);
							token = new Token(builder.toString(), Simbolos.SATRIBUICAO);
							nextChar = (char)reader.read();                            
						} else { // TODO perguntar para o freitas sobre espacos
							token = new Token(builder.toString(), Simbolos.SDOISPONTOS);
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
						nextChar = (char)reader.read();

					} else if (isRelational(nextChar)) {
						StringBuilder builder = new StringBuilder();
						builder.append(nextChar);
						char prevChar = nextChar;
						nextChar = (char)reader.read();
						if (prevChar == '=') {
							token = new Token(builder.toString(), Simbolos.SIG);                            
						} else if (prevChar == '>') {
							if (nextChar == '=') {
								builder.append(nextChar);
								token = new Token(builder.toString(), Simbolos.SMAIORIG);
								nextChar = (char)reader.read();
							} else {
								token = new Token(builder.toString(), Simbolos.SMAIOR);                                
							}
						} else {
							if (nextChar == '=') {
								builder.append(nextChar);
								token = new Token(builder.toString(), Simbolos.SMENORIG);
								nextChar = (char)reader.read();
							} else {
								token = new Token(builder.toString(), Simbolos.SMENOR);                                
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
						nextChar = (char)reader.read();

					} else {
						// Erro 
						System.out.println("Caractere inválido. Linha: " + lineNumber);
						break;
					}

				}

				// Insere Token na lista
				if (token != null) {
					mTokenList.add(token);                        
				}                
			}
			reader.close();
		}
	}

    private boolean isNumber(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        return false;
    }

    // TODO perguntar para o freitas se e case sensitive
    private boolean isLetter(char c) {
        if (c >= 'a' && c <= 'z') {
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
        if (c == '>' || c == '<' || c == '=') {
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
