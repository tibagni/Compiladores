package comp.app.alg;

import comp.app.Symbols;
import comp.app.Token;
import comp.app.error.CompilerError;
import comp.app.log.C_Log;

public class Syntactic extends Algorithm {
    private Tokens mTokenList = Tokens.getInstance();
    private Token mCurrentToken;
    private int mLabel;
    private Thread mLexicalThread;

    public Syntactic(Thread lexicalThread) {
        mLexicalThread = lexicalThread;
    }

    public CompilerError execute() {
        mLabel = 1;
        CompilerError error = CompilerError.NONE();

        // O primeiro token esperado e o 'programa'
        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SPROGRAMA) {
            return CompilerError.instantiateError(CompilerError.INVALID_PROGRAM_START, 0, 0);
        }

        // Le proximo token se a inicializacao do codigo esta correta
        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SIDENTIFICADOR) {
            // Um identificador era esperado aqui
            return CompilerError.instantiateError(CompilerError.INVALID_PROGRAM_NAME, 0, 0);
        }
        // Identificador encontrado!
        // TODO inserir na tabela de simbolos
        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SPONTO_VIRGULA) {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            return CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION,
            		line, col);
        }

        error = analyseBlock();
        if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
            if (mCurrentToken == null
                    || mCurrentToken.getSymbol() != Symbols.SPONTO) {
                return CompilerError.instantiateError(CompilerError.ILLEGAL_END_PROGRAM, 0, 0);
            }

            // Se a thread do analisador lexico ainda nao terminou espera!
            if (mLexicalThread != null && mLexicalThread.isAlive()) {
                try {
                    mLexicalThread.join();
                } catch (InterruptedException e) {
                    C_Log.error("Erro enquanto espera a thread que executa" +
                    		" o analisador lexical terminar", e);
                    return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
                }
            }
            if (!mTokenList.isLexicalFinishedWithouError()) {
            	// ErroLexico - UnknownError
            	return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
            }
        }

        return error;
    }

    private CompilerError analyseBlock() {
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
                            		line, col);
                            // Sai do loop e retorna o erro
                            break;
                        }
                    } else {
                        // Se deu erro, sai do loop e retorna
                        break;
                    }
                }
            } else {
                // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                error = CompilerError.instantiateError(CompilerError.ILLEGAL_VAR_DECLARATION, line,
                        col);
            }
        } else if (mCurrentToken == null && error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
            // ErroLexico - UnknownError
            error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
        }

        return error;
    }

    private CompilerError processVariables() {
        // Usado para informar linha e coluna do erro se o token atual for nulo
        Token lastToken = mCurrentToken;
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
                lastToken = mCurrentToken;
                if (mCurrentToken.getSymbol() == Symbols.SVIRGULA) {
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                    if (mCurrentToken != null) {
                        if (mCurrentToken.getSymbol() == Symbols.SDOISPONTOS) {
                            error = CompilerError.instantiateError(CompilerError.ILLEGAL_VAR_TYPE_DECLARATION,
                            		mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
                            break;
                        }
                    } else {
                        // Token Null - Unknown error
                        error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
                        break;
                    }
                }
            }

        } while (mCurrentToken.getSymbol() != Symbols.SDOISPONTOS);
        if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseType();
        }

        return error;
    }

    private CompilerError analyseType() {
        CompilerError error = CompilerError.NONE();
        if (mCurrentToken == null || (mCurrentToken.getSymbol() != Symbols.SINTEIRO &&
                mCurrentToken.getSymbol() != Symbols.SBOOLEANO)) {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            error = CompilerError.instantiateError(CompilerError.UNKNOWN_TYPE,
            		line, col);
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
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
        }

        while (mCurrentToken.getSymbol() == Symbols.SPROCEDIMENTO || mCurrentToken.getSymbol() == Symbols.SFUNCAO) {
        	if (mCurrentToken.getSymbol() == Symbols.SPROCEDIMENTO) {
        		error = processPorcDeclaration();
        	} else {
        		error = processFuncDeclaration();
        	}

        	if (error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;

        	if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA) {
        		mCurrentToken = mTokenList.getTokenFromBuffer();
        	} else {
        	    // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
        	    int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
        	    int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
        		error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION,
        		        line, col);
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
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
        }

        // TODO Nivel = L (marca ou novo galho)

        if (mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
        	// TODO Pesquisa declaracao1 do procedimento na tabela
        	//if (/* TODO se não encontrou na tabela */) {
        		 // TODO Insere_tabela(token.lexema,”procedimento”,nível, rótulo)
                 // {guarda na TabSimb}
        		 // TODO Gera(rotulo,NULL,´        ´,´            ´)
        		 // {CALL irá buscar este rótulo na TabSimb}
        		 mLabel++;

        		 mCurrentToken = mTokenList.getTokenFromBuffer();
        		 if (mCurrentToken != null && (mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA)) {
        			 error = analyseBlock();
        		 } else {
                     // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                     int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                     int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
             		error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION,
    	            		line, col);
        		 }
        	//} else {
            	// TODO error = procedimento ja foi declarado; semantico
        	//}
        } else {
     		error = CompilerError.instantiateError(CompilerError.ILLEGAL_PROC_FUNC_DECLARATION,
            		mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
        }
        // TODO Desempilha ou volta de nivel
        return error;
    }

    private CompilerError processFuncDeclaration() {
        CompilerError error = CompilerError.NONE();

        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null) {
            // Token null - UnknownError
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
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
                                 //função inteiro
        				} else {
       				 		// TODO então TABSIMB[pc].tipo:=
                        		//função booleana
        				}
        				mCurrentToken = mTokenList.getTokenFromBuffer();
        				if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA) {
        					error = analyseBlock();
        				} else {
        	                // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
        	                int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
        	                int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            				error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION, line,
            						col);
        				}
        			} else {
                        // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                        int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                        int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
        				error = CompilerError.instantiateError(CompilerError.UNKNOWN_RETURN_TYPE, line,
        						col);
        			}
        		} else {
                    // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                    int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                    int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
    				error = CompilerError.instantiateError(CompilerError.ILLEGAL_RETURN_TYPE_DECLARATION, line,
    						col);
        		}
        	} else {
        		// TODO erro semantico - funcao duplicada, ja foi declarada
        	}
        } else {
     		error = CompilerError.instantiateError(CompilerError.ILLEGAL_PROC_FUNC_DECLARATION,
            		mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
        }
        // TODO Desempilha ou volta de nive
        return error;
    }

    private CompilerError processCommands() {
        CompilerError error = CompilerError.NONE();

        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SINICIO) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = processSimpleCommand();

            if (error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;

            if (mCurrentToken == null) {
                return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
            }

            while (mCurrentToken.getSymbol() != Symbols.SFIM) {
                if (error.getErrorCode() != CompilerError.NONE_ERROR_CODE) break;
            	if (mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA) {
            		mCurrentToken = mTokenList.getTokenFromBuffer();
            		if (mCurrentToken != null && mCurrentToken.getSymbol() != Symbols.SFIM) {
            			error = processSimpleCommand();
            		} else if (mCurrentToken == null) {
            		    // Token null - erro
            		    return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
            		}
            	} else {
             		return CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION,
             		       mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
            	}
            }
            mCurrentToken = mTokenList.getTokenFromBuffer();
        } else {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
     		error = CompilerError.instantiateError(CompilerError.ILLEGAL_CMD_BLOCK_DECLARATION,
            		line, col);
        }

        return error;
    }

    private CompilerError processSimpleCommand() {
        CompilerError error = CompilerError.NONE();

        if (mCurrentToken == null) {
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
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
                    error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
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
        error = analyseExpression();

        return error;
    }

    private CompilerError processProcCall() {
        CompilerError error = CompilerError.NONE();

        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SIDENTIFICADOR) {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            error = CompilerError.instantiateError(CompilerError.INVALID_PROC_FUNC_NAME, line, col);
        }

        return error;
    }

    private CompilerError processFuncCall() {
        CompilerError error = CompilerError.NONE();

        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SIDENTIFICADOR) {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            error = CompilerError.instantiateError(CompilerError.INVALID_PROC_FUNC_NAME, line, col);
        }

        return error;
    }

    private CompilerError processIf() {
        CompilerError error = CompilerError.NONE();

        mCurrentToken = mTokenList.getTokenFromBuffer();
        error = analyseExpression();
        // Nao continua se a analise da expressao ja falhou!
        if (error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;

        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SENTAO) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = processSimpleCommand();
            if (mCurrentToken == null) {
                return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
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
            error = CompilerError.instantiateError(CompilerError.MALFORMED_IF_CONSTRUCTION, line, col);
        }

        return error;
    }

    private CompilerError analyseExpression() {
        CompilerError error = CompilerError.NONE();

        error = analyseSimpleExpression();

        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
        }

        if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE
                && (mCurrentToken.getSymbol() == Symbols.SMAIOR
                        || mCurrentToken.getSymbol() == Symbols.SMAIORIG
                        || mCurrentToken.getSymbol() == Symbols.SIG
                        || mCurrentToken.getSymbol() == Symbols.SMENOR
                        || mCurrentToken.getSymbol() == Symbols.SMENORIG || mCurrentToken
                        .getSymbol() == Symbols.SDIF)) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseSimpleExpression();
        }
        return error;
    }

    private CompilerError analyseSimpleExpression() {
        CompilerError error = CompilerError.NONE();
        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
        }

        if (mCurrentToken.getSymbol() == Symbols.SMAIS || mCurrentToken.getSymbol() == Symbols.SMENOS) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
        }

        error = analyseTerm();
        if (error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;
        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
        }

        while (mCurrentToken.getSymbol() == Symbols.SMAIS || mCurrentToken.getSymbol() == Symbols.SMENOS
                || mCurrentToken.getSymbol() == Symbols.SOU) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseTerm();
            if (mCurrentToken == null) {
                // Token null - erro
                error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
            }
            if (error.getErrorCode() != CompilerError.NONE_ERROR_CODE) break;
        }
        return error;
    }

    private CompilerError analyseTerm() {
        CompilerError error = CompilerError.NONE();

        error = analyseFactor();
        if(error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;

        while (mCurrentToken.getSymbol() == Symbols.SMULT || mCurrentToken.getSymbol() == Symbols.SDIV
                || mCurrentToken.getSymbol() == Symbols.SE) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseFactor();
        }
        return error;
    }

    private CompilerError analyseFactor() {
        CompilerError error = CompilerError.NONE();
        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR_CODE, 0, 0);
        }

        if(mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
            //if(mLabel == 1) {// TODO pesquisa_tabela(token.lexema,nível,ind
                //if(mLabel == 1/* TODO TabSimb[ind].tipo = “função inteiro”) ou
                //        (TabSimb[ind].tipo = “função booleano”*/) {
                    //error = processFuncCall();
                    //if(error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;
                //} else {
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                //}
            //} else {
                //TODO erro semantico
            //}
        } else if(mCurrentToken.getSymbol() == Symbols.SNUMERO) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
        } else if(mCurrentToken.getSymbol() == Symbols.SNAO) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseFactor();
        } else if(mCurrentToken.getSymbol() == Symbols.SABRE_PARENTESES) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseExpression();
            // Se falhou, nao continua
            if(error.getErrorCode() != CompilerError.NONE_ERROR_CODE) return error;

            if(mCurrentToken.getSymbol() == Symbols.SFECHA_PARENTESES) {
                mCurrentToken = mTokenList.getTokenFromBuffer();
            } else {
                error = CompilerError.instantiateError(CompilerError.MALFORMED_EXPRESSION,
                        mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
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
        error = analyseExpression();
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
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            return CompilerError.instantiateError(CompilerError.MALFORMED_WHILE_CONSTRUCTION, line, col);
        }

        return error;
    }

    private CompilerError doRead() {
        CompilerError error = CompilerError.NONE();
        mCurrentToken = mTokenList.getTokenFromBuffer();

        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SABRE_PARENTESES) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
                //if (/* TODO se variavel esta na tabela (token.lexema)*/) {
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                    if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SFECHA_PARENTESES) {
                        mCurrentToken = mTokenList.getTokenFromBuffer();
                    } else {
                        // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                        int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                        int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                        error = CompilerError.instantiateError(CompilerError.MISSING_CLOSE_PARENTHESIS,
                                line, col);
                    }
                //} else {
                    // TODO erro semantico
                //}
            } else {
                // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                error = CompilerError.instantiateError(CompilerError.WRONG_READ_WRITE_ARGUMENT,
                        line, col);
            }
        } else {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            error = CompilerError.instantiateError(CompilerError.OPEN_PARENTHESIS_EXPECTED,
                    line, col);
        }

        return error;
    }

    private CompilerError doWrite() {
        CompilerError error = CompilerError.NONE();
        mCurrentToken = mTokenList.getTokenFromBuffer();

        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SABRE_PARENTESES) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
                //if (/* TODO se variavel ou funcao esta na tabela (token.lexema)*/) {
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                    if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SFECHA_PARENTESES) {
                        mCurrentToken = mTokenList.getTokenFromBuffer();
                    } else {
                        // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                        int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                        int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                        error = CompilerError.instantiateError(CompilerError.MISSING_CLOSE_PARENTHESIS,
                                line, col);
                    }
                //} else {
                    // TODO erro semantico
                //}
            } else {
                // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                error = CompilerError.instantiateError(CompilerError.WRONG_READ_WRITE_ARGUMENT,
                        line, col);
            }
        } else {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            error = CompilerError.instantiateError(CompilerError.OPEN_PARENTHESIS_EXPECTED,
                    line, col);
        }

        return error;
    }
}
