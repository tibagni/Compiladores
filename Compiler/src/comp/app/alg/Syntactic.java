package comp.app.alg;

import comp.app.Symbols;
import comp.app.Token;
import comp.app.error.CompilerError;

public class Syntactic extends Algorithm {
    private Tokens mTokenList = Tokens.getInstance();
    private Token mCurrentToken;
    private int mLabel;

    public CompilerError execute() {
        mLabel = 1;
        CompilerError error = CompilerError.NONE();
        
        // TODO verificar se Lexico esta rodando ou se ja falhou
        // antes de requisitar o primeiro token
        
        // O primeiro token esperado e o 'programa'
        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SPROGRAMA) {
            return CompilerError.instantiateError(CompilerError.INVALID_PROGRAM_START, 0, 0, this);
        }
        
        // Le proximo token se a inicializacao do codigo esta correta
        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SIDENTIFICADOR) {
            // Um identificador era esperado aqui
            return CompilerError.instantiateError(CompilerError.INVALID_PROGRAM_NAME, 0, 0, this);
        }
        // Identificador encontrado!
        // TODO inserir na tabela de simbolos
        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SPONTO_VIRGULA) {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            return CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION, 
            		line, col, this);
        }
        
        error = blockAnalyser();
        if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
            if (mCurrentToken == null
                    || mCurrentToken.getSymbol() != Symbols.SPONTO) {
                return CompilerError.instantiateError(CompilerError.ILLEGAL_END_PROGRAM, 0, 0, this);
            }

            if (!mTokenList.isLexicalFinishedWithouError()) {
            	// ErroLexico - UnknownError
            	return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
            }
        }

        return error;
    }

    private CompilerError blockAnalyser() {
        CompilerError error = CompilerError.NONE();
        mCurrentToken = mTokenList.getTokenFromBuffer();

        error = processVarDeclaration();
        if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
            error = processSubRoutine();
        }
        if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
            error = processCommands();
        }

        return error;
    }

    private CompilerError processVarDeclaration() {
        CompilerError error = CompilerError.NONE();
        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SVAR) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
                while (mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
                    error = processVariables();
                    if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
                        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA) {
                            mCurrentToken = mTokenList.getTokenFromBuffer();
                        } else {
                            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                            error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION, 
                            		line, col, this);
                        }
                    }
                }
            } else {
                // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                error = CompilerError.instantiateError(CompilerError.ILLEGAL_DECLARATION, line,
                        col, this);
            }
        } else if (mCurrentToken == null) {
            // ErroLexico - UnknownError
            error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);            
        }

        return error;
    }

    private CompilerError processVariables() {
        CompilerError error = CompilerError.NONE();
        do {
            // TODO procura variavel duplicada na tabela de simbolos
//            if (tabelaSimbolos.contains(mCurrentToken.getLexema())) {
//                return erro; DuplicatedVar
//            }
            
            // TODO inserir variavel na tabela de simbolos
            mCurrentToken = mTokenList.getTokenFromBuffer();
            if (mCurrentToken != null && (mCurrentToken.getSymbol() == Symbols.SVIRGULA ||
                    mCurrentToken.getSymbol() == Symbols.SDOISPONTOS)) {
                if (mCurrentToken.getSymbol() == Symbols.SVIRGULA) {
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                    if (mCurrentToken != null) {
                        if (mCurrentToken.getSymbol() == Symbols.SDOISPONTOS) {
                            error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION, 
                            		mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn(), this);
                        }
                    } else {
                        // Token Null - Unknown error
                        error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
                        break;
                    }
                }
            } else {
                // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                error = CompilerError.instantiateError(CompilerError.ILLEGAL_DECLARATION, line,
                        col, this);
                break;
            }
            
        } while (mCurrentToken.getSymbol() == Symbols.SDOISPONTOS);
        if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = typeAnalyser();
        }

        return error;
    }

    private CompilerError typeAnalyser() {
        CompilerError error = CompilerError.NONE();
        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SINTEIRO ||
                mCurrentToken.getSymbol() != Symbols.SBOOLEANO) {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            error = CompilerError.instantiateError(CompilerError.UNKNOWN_TYPE, 
            		line, col, this);
        } else {
            // TODO coloca tipo na tabela de simbolos
            mCurrentToken = mTokenList.getTokenFromBuffer();
        }
            
        return error;        
    }

    private CompilerError processSubRoutine() {
        CompilerError error = CompilerError.NONE();
        int label = mLabel;
        // TODO GERA('', JMP rotulo, '');
        mLabel++;

        if (mCurrentToken == null) {
            // Token null - UnknownError
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
        }

        while (mCurrentToken.getSymbol() == Symbols.SPROCEDIMENTO || mCurrentToken.getSymbol() == Symbols.SFUNCAO) {
        	if (mCurrentToken.getSymbol() == Symbols.SPROCEDIMENTO) {
        		error = processPorcDeclaration();
        	} else {
        		error = processFuncDeclaration();
        	}

        	if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA) {
        		mCurrentToken = mTokenList.getTokenFromBuffer();
        	} else {
        	    // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
        	    int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
        	    int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
        		error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION, 
        		        line, col, this);
        	}
        }
        // TODO GERA(label, null, '', '')
        return error;
    }

    private CompilerError processPorcDeclaration() {
        CompilerError error = CompilerError.NONE();

        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null) {
            // Token null - UnknownError
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
        }

        // TODO Nivel = L (marca ou novo galho)
        
        if (mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
        	// TODO Pesquisa declaracao1 do procedimento na tabela
        	if (mLabel == 3/* TODO se não encontrou na tabela */) {
        		 // TODO Insere_tabela(token.lexema,”procedimento”,nível, rótulo)             
                 // {guarda na TabSimb}
        		 // TODO Gera(rotulo,NULL,´        ´,´            ´)           
        		 // {CALL irá buscar este rótulo na TabSimb}              
        		 mLabel++;

        		 mCurrentToken = mTokenList.getTokenFromBuffer();
        		 if (mCurrentToken != null && (mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA)) {
        			 error = blockAnalyser();
        		 } else {
                     // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                     int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                     int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
             		error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION, 
    	            		line, col, this); 
        		 }
        	} else {
            	// TODO error = procedimento ja foi declarado;
        	}
        } else {
     		error = CompilerError.instantiateError(CompilerError.ILLEGAL_DECLARATION, 
            		mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn(), this); 
        }
        // TODO Desempilha ou volta de nivel
        return error;    	
    }

    private CompilerError processFuncDeclaration() {
        CompilerError error = CompilerError.NONE();

        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null) {
            // Token null - UnknownError
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
        }

        // TODO Nivel = L (marca ou novo galho)

        if (mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
        	// TODO Pesquisa declaracao1 do procedimento na tabela
        	if (mLabel == 3/* TODO se não encontrou na tabela */) {
       		 	// TODO Insere_tabela(token.lexema,””,nível, rótulo)
        		mCurrentToken = mTokenList.getTokenFromBuffer();
        		if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SDOISPONTOS) {
        			mCurrentToken = mTokenList.getTokenFromBuffer();
        			if (mCurrentToken != null && (mCurrentToken.getSymbol() == Symbols.SINTEIRO ||
        					mCurrentToken.getSymbol() == Symbols.SBOOLEANO)) {
        				if (mCurrentToken.getSymbol() == Symbols.SINTEIRO) { 
        					// TODO então TABSIMB[pc].tipo:=  
                                 //“função inteiro
        				} else {
       				 		// TODO então TABSIMB[pc].tipo:=  
                        		//“função booleana
        				}
        				mCurrentToken = mTokenList.getTokenFromBuffer();
        				if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA) {
        					error = blockAnalyser();
        				} else {
        	                // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
        	                int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
        	                int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            				error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION, line,
            						col, this); 
        				}
        			} else {
                        // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                        int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                        int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
        				error = CompilerError.instantiateError(CompilerError.UNKNOWN_TYPE, line,
        						col, this);
        			}
        		} else {
                    // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                    int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                    int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
    				error = CompilerError.instantiateError(CompilerError.ILLEGAL_DECLARATION, line,
    						col, this);        			
        		}
        	} else {
        		// TODO erro semantico - funcao duplicada, ja foi declarada
        	}
        } else {
     		error = CompilerError.instantiateError(CompilerError.ILLEGAL_DECLARATION, 
            		mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn(), this); 
        }
        // TODO Desempilha ou volta de nive
        return error;    	
    }

    private CompilerError processCommands() {
        CompilerError error = CompilerError.NONE();

        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SINICIO) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = processSimpleCommand();
            if (mCurrentToken == null) {
                return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
            }
            
            while (mCurrentToken.getSymbol() != Symbols.SFIM) {
            	if (mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA) {
            		mCurrentToken = mTokenList.getTokenFromBuffer();
            		if (mCurrentToken != null && mCurrentToken.getSymbol() != Symbols.SFIM) {
            			error = processSimpleCommand();
            		} else if (mCurrentToken == null) {
            		    // Token null - erro
            		    return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
            		}
            	} else {
             		error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION, 
             		       mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn(), this); 
            	}
            }
            mCurrentToken = mTokenList.getTokenFromBuffer();
        } else {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
     		error = CompilerError.instantiateError(CompilerError.ILLEGAL_DECLARATION, 
            		line, col, this); 
        }

        return error;
    }

    private CompilerError processSimpleCommand() {
        CompilerError error = CompilerError.NONE();
        
        if (mCurrentToken == null) {
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
        }
        
        switch(mCurrentToken.getSymbol()) {
        	case Symbols.SIDENTIFICADOR:
        	    mCurrentToken = mTokenList.getTokenFromBuffer();
                if (mCurrentToken != null) {
                    if (mCurrentToken.getSymbol() == Symbols.SATRIBUICAO) {
                        error = processAttr();
                    } else {
                        error = processProcCall();
                    }
                } else {
                    error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
                }
        	    break;
        	case Symbols.SSE:
        	    error = processIf();
        	    break;
        	case Symbols.SENQUANTO:
        	    error = processWhile();
        	    break;
        	case Symbols.SLEIA:
        	    error = doRead();
        	    break;
        	case Symbols.SESCREVA:
        	    error = doWrite();
        	    break;
        	default:
        	    error = processCommands();
        }
    	
        return error;
    }

    private CompilerError processAttr() {
        CompilerError error = CompilerError.NONE();
        mCurrentToken = mTokenList.getTokenFromBuffer();
        error = expressionAnalyser();
        
        return error;
    }

    private CompilerError processProcCall() {
        CompilerError error = CompilerError.NONE();
        
        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SIDENTIFICADOR) {
            // TODO erro - nome de procedimento/funcao invalido
        }
        
        return error;
    }

    private CompilerError processFuncCall() {
        CompilerError error = CompilerError.NONE();
        
        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SIDENTIFICADOR) {
            // TODO erro - nome de procedimento/funcao invalido
        }
        
        return error;
    }

    private CompilerError processIf() {
        CompilerError error = CompilerError.NONE();

        mCurrentToken = mTokenList.getTokenFromBuffer();
        error = expressionAnalyser();
        // Nao continua se a analise da expressao ja falhou!
        if (error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;

        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SENTAO) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = processSimpleCommand();
            if (mCurrentToken == null) {
                return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
            }
            if (error.getErrorCode() != CompilerError.NONE_ERROR_CODE &&
                    mCurrentToken.getSymbol() == Symbols.SSENAO) {
                mCurrentToken = mTokenList.getTokenFromBuffer();
                error = processSimpleCommand();
            }
        } else {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            // TODO error
        }

        return error;
    }
    
    private CompilerError expressionAnalyser() {
        CompilerError error = CompilerError.NONE();

        error = simpleExpressionAnalyser();

        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
        }

        if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE
                && (mCurrentToken.getSymbol() == Symbols.SMAIOR
                        || mCurrentToken.getSymbol() == Symbols.SMAIORIG
                        || mCurrentToken.getSymbol() == Symbols.SIG
                        || mCurrentToken.getSymbol() == Symbols.SMENOR
                        || mCurrentToken.getSymbol() == Symbols.SMENORIG || mCurrentToken
                        .getSymbol() == Symbols.SDIF)) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = simpleExpressionAnalyser();
        }
        return error;
    }
    
    private CompilerError simpleExpressionAnalyser() {
        CompilerError error = CompilerError.NONE();
        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
        }
        
        if(mCurrentToken.getSymbol() == Symbols.SMAIS || mCurrentToken.getSymbol() == Symbols.SMENOS) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = termAnalyser();
            if(error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;
            if (mCurrentToken == null) {
                // Token null - erro
                return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
            }
            
            while(mCurrentToken.getSymbol() == Symbols.SMAIS || mCurrentToken.getSymbol() == Symbols.SMENOS || 
                    mCurrentToken.getSymbol() == Symbols.SOU) {
                mCurrentToken = mTokenList.getTokenFromBuffer();
                error = termAnalyser();
                if (mCurrentToken == null) {
                    // Token null - erro
                    error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
                }
                if(error.getErrorCode() != CompilerError.NONE_ERROR_CODE) break;
            }
        }
        return error;
    }

    private CompilerError termAnalyser() {
        CompilerError error = CompilerError.NONE();
        
        error = factorAnalyser();
        if(error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;
        
        while (mCurrentToken.getSymbol() == Symbols.SMULT || mCurrentToken.getSymbol() == Symbols.SDIV 
                || mCurrentToken.getSymbol() == Symbols.SE) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
        }
        return error;
    }
    
    private CompilerError factorAnalyser() {
        CompilerError error = CompilerError.NONE();
        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0, this);
        }
        
        if(mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
            if(mLabel == 1) {// TODO pesquisa_tabela(token.lexema,nível,ind 
                if(mLabel == 1/* TODO TabSimb[ind].tipo = “função inteiro”) ou  
                        (TabSimb[ind].tipo = “função booleano”*/) {
                    error = processFuncCall();
                    if(error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;
                } else { 
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                }
            } else {
                //TODO erro semantico
            }
        } else if(mCurrentToken.getSymbol() == Symbols.SNUMERO) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
        } else if(mCurrentToken.getSymbol() == Symbols.SNAO) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = factorAnalyser();
        } else if(mCurrentToken.getSymbol() == Symbols.SABRE_PARENTESES) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = expressionAnalyser();
            // Se falhou, nao continua
            if(error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;

            if(mCurrentToken.getSymbol() == Symbols.SFECHA_PARENTESES) {
                mCurrentToken = mTokenList.getTokenFromBuffer();
            } else {
                error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION, 
                        mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn(), this);
            }
        } else {
            // TODO erro?!?
        }

        return error;
    }
    
    private CompilerError processWhile() {
        CompilerError error = CompilerError.NONE();
        int label, label2;
        label = mLabel;
        // Gera(rotulo,NULL,´             ´,´               ´)      {início do while}
        mLabel++;

        mCurrentToken = mTokenList.getTokenFromBuffer();
        error = expressionAnalyser();
        // Nao continua se a analise da expressao ja falhou!
        if (error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;

        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SFACA) {
            label2 = mLabel;
            //Gera(´                ´,JMPF,rotulo,´               ´)          {salta se falso} 
            mLabel++;

            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = processSimpleCommand();
            // Gera(´           ´,JMP,auxrot1,´               ´)   {retorna início loop} 
            // Gera(auxrot2,NULL,´             ´,´               ´)   {fim do while}
        } else {
            // TODO erro de while.
        }
        
        return error;
    }

    private CompilerError doRead() {
        CompilerError error = CompilerError.NONE();
        mCurrentToken = mTokenList.getTokenFromBuffer();
        
        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SABRE_PARENTESES) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
                if (mLabel == 3 /* TODO se variavel esta na tabela (token.lexema)*/) {
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                    if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SFECHA_PARENTESES) {
                        mCurrentToken = mTokenList.getTokenFromBuffer();
                    } else {
                        // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                        int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                        int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                        error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION,
                                line, col, this);
                    }
                } else {
                    // TODO erro semantico
                }
            } else {
                // Erro - nao e identificador
            }
        } else {
            // TODO erro leia!
        }
        
        return error;
    }

    private CompilerError doWrite() {
        CompilerError error = CompilerError.NONE();
        mCurrentToken = mTokenList.getTokenFromBuffer();
        
        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SABRE_PARENTESES) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
                if (mLabel == 3 /* TODO se variavel ou funcao esta na tabela (token.lexema)*/) {
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                    if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SFECHA_PARENTESES) {
                        mCurrentToken = mTokenList.getTokenFromBuffer();
                    } else {
                        // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                        int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                        int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                        error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION,
                                line, col, this);
                    }
                } else {
                    // TODO erro semantico
                }
            } else {
                // TODO erro, nao e identificador
            }
        } else {
            // TODO erro escreva
        }
        
        return error;
    }
}
