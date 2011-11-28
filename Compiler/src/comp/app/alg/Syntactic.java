package comp.app.alg;

import java.util.ArrayList;

import comp.app.Symbols;
import comp.app.Token;
import comp.app.alg.Semantic.ExpressionElement;
import comp.app.alg.Semantic.SymbolTableEntry;
import comp.app.error.CompilerError;
import comp.app.error.InvalidExpressionException;
import comp.app.log.C_Log;

/*
 * *********************************CONSIDERACOES********************************
 *
 * 1 - Quando um erro ocorre durante a analise lexical, esta thread e interrompida
 * e, se for interrompida no momento da leitura de um token, o token retornado sera nulo
 *
 * 2 - Quando ocorrer um erro durante a analise lexical, nao importa qual erro sera retornado
 * pois, o erro que sera levado em conta sera o erro lexical. Por isso e usado UNKNOWN_ERROR
 * quando um token e nulo (quando esta thread for interrompida no momento da leitura do token)
 */

public class Syntactic {
    private Tokens mTokenList = Tokens.getInstance();
    private Token mCurrentToken;
    private int mLabel;
    private Thread mLexicalThread;
    private Semantic mSemantic;
    private CodeGenerator mCodeGenerator;

    /** Variavel utilizada para que uma expressao possa ser
     * analisada pelo semantico e geracao de codigo
     */
    private ArrayList<ExpressionElement> mCurrentExpression;

    private SymbolTableEntry mEntry;

    private int mVarBaseAddress;

    public Syntactic(Thread lexicalThread) {
        mLexicalThread = lexicalThread;
        mSemantic = Semantic.getInstance();
        mCodeGenerator = CodeGenerator.getInstance();
        mEntry = null;
        mCurrentExpression = new ArrayList<ExpressionElement>();
        mVarBaseAddress = 0;
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
        // Insere na tabela de simbolos o nome do programa
        mEntry = new SymbolTableEntry();
        mEntry.mType = SymbolTableEntry.TYPE_PROGRAM_NAME;
        mEntry.mLexema = mCurrentToken.getLexema();
        mEntry.mLevel = SymbolTableEntry.SCOPE_MARK; // Escopo mais externo do programa
        mSemantic.pushToSymbolTable(mEntry);

        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SPONTO_VIRGULA) {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            return CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION,
            		line, col);
        }

        //[GERACAO DE CODIGO]
        mCodeGenerator.appendCode("START");
        //[GERACAO DE CODIGO]

        int varAddressBefore = mVarBaseAddress;
        error = analyseBlock();
        //[GERACAO DE CODIGO]
        int varDeclarationCount = mVarBaseAddress - varAddressBefore;
        if (varDeclarationCount > 0) {
            mVarBaseAddress -= varDeclarationCount;
            mCodeGenerator.appendCode("DALLOC " + mVarBaseAddress + " " + varDeclarationCount);
        }
        //[GERACAO DE CODIGO]
        if (error.getErrorCode() == CompilerError.NONE_ERROR) {
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
                    return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
                }
            }
            if (!mTokenList.isLexicalFinishedWithouError()) {
            	// ErroLexico - UnknownError
            	return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
            }
            //[GERACAO DE CODIGO]
            mCodeGenerator.appendCode("HLT");
            //[GERACAO DE CODIGO]
        }

        return error;
    }

    private CompilerError analyseBlock() {
        CompilerError error = CompilerError.NONE();
        mCurrentToken = mTokenList.getTokenFromBuffer();
        int varAddressBefore = mVarBaseAddress;
        int allocPosition = -1;

        error = processVarDeclaration();
        int varDeclarationCount = mVarBaseAddress - varAddressBefore;
        //[GERACAO DE CODIGO]
        if (varDeclarationCount > 0) {
          allocPosition = mCodeGenerator.appendCode("ALLOC " + varAddressBefore + " " + varDeclarationCount);
        }
        //[GERACAO DE CODIGO]

        if (error.getErrorCode() == CompilerError.NONE_ERROR) {
            error = processSubRoutine(allocPosition);
        }
        if (error.getErrorCode() == CompilerError.NONE_ERROR) {
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
                    if (error.getErrorCode() == CompilerError.NONE_ERROR) {
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
        } else if (mCurrentToken == null && error.getErrorCode() == CompilerError.NONE_ERROR) {
            // ErroLexico - UnknownError
            error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
        }
        if (error.getErrorCode() != CompilerError.NONE_ERROR) return error;

        return error;
    }

    private CompilerError processVariables() {
        CompilerError error = CompilerError.NONE();
        do {
            //[ANALISE SEMANTICA]
            // Procura variavel duplicada na tabela de simbolos
            if (mSemantic.isVarAlreadyDeclaredInScope(mCurrentToken.getLexema())) {
                return CompilerError.instantiateError(CompilerError.DUPLICATED_VAR,
                        mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
            }

            // Variavel nao e duplicada, inserir na tabela de simbolos
            mEntry = new SymbolTableEntry();
            mEntry.mLexema = mCurrentToken.getLexema();
            mEntry.mType = SymbolTableEntry.TYPE_VARIABLE;
            mEntry.mAddress = mVarBaseAddress++; // Incrementa o endereco para o proximo registro
            mSemantic.pushToSymbolTable(mEntry);
            //[ANALISE SEMANTICA]

            mCurrentToken = mTokenList.getTokenFromBuffer();
            if (mCurrentToken != null && (mCurrentToken.getSymbol() == Symbols.SVIRGULA ||
                    mCurrentToken.getSymbol() == Symbols.SDOISPONTOS)) {
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
                        error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
                        break;
                    }
                }
            } else {
                // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                error = CompilerError.instantiateError(CompilerError.MALFORMED_VAR_DECLARATION, line, col);
                break;
            }
        } while (mCurrentToken.getSymbol() != Symbols.SDOISPONTOS);
        if (error.getErrorCode() == CompilerError.NONE_ERROR) {
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
            //[ANALISE SEMANTICA]
            // Coloca tipo na tabela de simbolos para todas as variaveis na tabela sem tipo
            mSemantic.populateVarType(mCurrentToken.getSymbol());
            //[ANALISE SEMANTICA]

            mCurrentToken = mTokenList.getTokenFromBuffer();
        }

        return error;
    }

    private CompilerError processSubRoutine(int allocPos) {
        CompilerError error = CompilerError.NONE();
        int label = mLabel;
        //[GERACAO DE CODIGO]
        int pos = mCodeGenerator.appendCode("JMP " + label);
        //[GERACAO DE CODIGO]
        mLabel++;
        int newAllocPos = pos - 1; // Onde a funcao ira alocar seu valor de retorno caso
                                   // Nenhuma variavel tenha sido alocada em seu escopo exterior

        if (mCurrentToken == null) {
            // Token null - UnknownError
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
        }

        while (mCurrentToken.getSymbol() == Symbols.SPROCEDIMENTO || mCurrentToken.getSymbol() == Symbols.SFUNCAO) {
        	if (mCurrentToken.getSymbol() == Symbols.SPROCEDIMENTO) {
        		error = processPorcDeclaration();
        	} else {
        		error = processFuncDeclaration(allocPos, newAllocPos);
        	}

        	if (error.getErrorCode() != CompilerError.NONE_ERROR) return error;

        	if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA) {
        		mCurrentToken = mTokenList.getTokenFromBuffer();

        		if (mCurrentToken == null) {
                    error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
        		    break;
        		}
        	} else {
        	    // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
        	    int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
        	    int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
        		error = CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION,
        		        line, col);
        		break;
        	}
        }
        //[GERACAO DE CODIGO]
        mCodeGenerator.appendCode(label + " NULL");
        //[GERACAO DE CODIGO]
        return error;
    }

    private CompilerError processPorcDeclaration() {
        CompilerError error = CompilerError.NONE();

        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null) {
            // Token null - UnknownError
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
        }

        int varAddressBefore = mVarBaseAddress;
        if (mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
            //[ANALISE SEMANTICA]
        	// Pesquisa declaracao do procedimento na tabela
        	if (!mSemantic.isSubRoutineAlreadyDeclaredInScope(mCurrentToken.getLexema())) {
        	    mEntry = new SymbolTableEntry();
        	    mEntry.mLevel = SymbolTableEntry.SCOPE_MARK;
        	    mEntry.mLabel = mLabel++;
        	    mEntry.mLexema = mCurrentToken.getLexema();
        	    mEntry.mType = SymbolTableEntry.TYPE_PROCEDURE;
        	    mSemantic.pushToSymbolTable(mEntry);
                //[ANALISE SEMANTICA]

                // CALL irá buscar este rótulo (mLabel) na TabSimb
                //[GERACAO DE CODIGO]
                mCodeGenerator.appendCode(mEntry.mLabel + " NULL");
                //[GERACAO DE CODIGO]

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
        	} else {
            	 error = CompilerError.instantiateError(CompilerError.DUPLICATED_SUB_ROUTINE,
            	         mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
        	}
        } else {
     		error = CompilerError.instantiateError(CompilerError.ILLEGAL_PROC_FUNC_DECLARATION,
            		mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
        }
        // Desempilha - volta de nivel
        mSemantic.popEverythingUntilScopeMark();
        //[GERACAO DE CODIGO]
        int varDeclarationCount = mVarBaseAddress - varAddressBefore;
        if (varDeclarationCount > 0) {
            mVarBaseAddress -= varDeclarationCount;
            mCodeGenerator.appendCode("DALLOC " + mVarBaseAddress + " " + varDeclarationCount);
        }
        mCodeGenerator.appendCode("RETURN");
        //[GERACAO DE CODIGO]
        return error;
    }

    private CompilerError processFuncDeclaration(int allocPos, int newAllocPos) {
        CompilerError error = CompilerError.NONE();

        // A primeira coisa antes de declarar uma funcao
        // e alocar espaco para seu valor de retorno
        if (allocPos > -1) {
            mCodeGenerator.incVarAlloc(allocPos);
        } else {
            // Nao foi alocada nenhuma variavel no nivel superior
            // Alocamos agora, antes de entrar na funcao
            mCodeGenerator.putCodeAt(newAllocPos, "ALLOC " + (mVarBaseAddress + 1) + " 1");
        }

        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null) {
            // Token null - UnknownError
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
        }

        int varAddressBefore = mVarBaseAddress;
        if (mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
            //[ANALISE SEMANTICA]
        	// Pesquisa declaracao da funcao na tabela
            if (!mSemantic.isSubRoutineAlreadyDeclaredInScope(mCurrentToken.getLexema())) {
                mEntry = new SymbolTableEntry();
                mEntry.mLevel = SymbolTableEntry.SCOPE_MARK;
                mEntry.mLabel = mLabel++;
                mEntry.mLexema = mCurrentToken.getLexema();
                mEntry.mType = SymbolTableEntry.TYPE_FUNCTION;
                mEntry.mAddress = mVarBaseAddress++; // Funcao tem este endereco para retorno
                mSemantic.pushToSymbolTable(mEntry);
                varAddressBefore++;
                //[ANALISE SEMANTICA]

                // CALL irá buscar este rótulo (mLabel) na TabSimb
                //[GERACAO DE CODIGO]
                mCodeGenerator.appendCode(mEntry.mLabel + " NULL");
                //[GERACAO DE CODIGO]

        		mCurrentToken = mTokenList.getTokenFromBuffer();
        		if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SDOISPONTOS) {
        			mCurrentToken = mTokenList.getTokenFromBuffer();
        			if (mCurrentToken != null && (mCurrentToken.getSymbol() == Symbols.SINTEIRO ||
        					mCurrentToken.getSymbol() == Symbols.SBOOLEANO)) {
        			    // Seta o tipo de retorno da funcao na tabela de simbolos
        			    mSemantic.setFunctionAtTopReturnType(mCurrentToken.getSymbol());
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
        		error = CompilerError.instantiateError(CompilerError.DUPLICATED_SUB_ROUTINE,
        		        mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
        	}
        } else {
     		error = CompilerError.instantiateError(CompilerError.ILLEGAL_PROC_FUNC_DECLARATION,
            		mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
        }
        // Desempilha - volta de nivel
        mSemantic.popEverythingUntilScopeMark();
        //[GERACAO DE CODIGO]
        int varDeclarationCount = mVarBaseAddress - varAddressBefore;
        if (varDeclarationCount > 0) {
            mVarBaseAddress -= varDeclarationCount;
            mCodeGenerator.appendCode("DALLOC " + mVarBaseAddress + " " + varDeclarationCount);
        }
        mCodeGenerator.appendCode("RETURN");
        //[GERACAO DE CODIGO]
        return error;
    }

    private CompilerError processCommands() {
        CompilerError error = CompilerError.NONE();

        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SINICIO) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = processSimpleCommand();

            if (error.getErrorCode() != CompilerError.NONE_ERROR) return error;

            if (mCurrentToken == null) {
                return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
            }

            while (mCurrentToken.getSymbol() != Symbols.SFIM) {
                if (error.getErrorCode() != CompilerError.NONE_ERROR) break;
            	if (mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA) {
            		mCurrentToken = mTokenList.getTokenFromBuffer();
            		if (mCurrentToken != null && mCurrentToken.getSymbol() != Symbols.SFIM) {
            			error = processSimpleCommand();
            		} else if (mCurrentToken == null) {
            		    // Token null - erro
            		    return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
            		}
            	} else {
                    return CompilerError.instantiateError(CompilerError.ILLEGAL_END_EXPRESSION,
                            mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
                }

                if (mCurrentToken == null) {
                    error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
                    break;
                }
            }
            mCurrentToken = mTokenList.getTokenFromBuffer();
        } else {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = 0;
            int col  = 0;
            int errorCode = CompilerError.ILLEGAL_CMD_BLOCK_DECLARATION;

            // Vamos tentar avaliar melhor o erro aqui!
            if (mCurrentToken != null) {
                line = mCurrentToken.getTokenLine();
                col = mCurrentToken.getTokenEndColumn();

                if (mCurrentToken.getSymbol() == Symbols.SPONTO_VIRGULA ||
                    mCurrentToken.getSymbol() == Symbols.SPONTO ||
                    mCurrentToken.getSymbol() == Symbols.SDOISPONTOS) {
                    errorCode = CompilerError.UNEXPECTED_TOKEN;
                }
            }
     		error = CompilerError.instantiateError(errorCode, line, col);
        }

        return error;
    }

    private CompilerError processSimpleCommand() {
        CompilerError error = CompilerError.NONE();

        if (mCurrentToken == null) {
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
        }

        switch(mCurrentToken.getSymbol()) {
        	case Symbols.SIDENTIFICADOR:
        	    Token id = mCurrentToken;
        	    mCurrentToken = mTokenList.getTokenFromBuffer();
                if (mCurrentToken != null) {
                    if (mCurrentToken.getSymbol() == Symbols.SATRIBUICAO) {
                        error = processAttr(id);
                    } else {
                        error = processProcCall(id);
                    }
                } else {
                    error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
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

    private CompilerError processAttr(Token var) {
        CompilerError error = CompilerError.NONE();
        mCurrentToken = mTokenList.getTokenFromBuffer();

        // Verifica se a variavel ja foi declarada
        if (!mSemantic.isIdentifierDeclared(var.getLexema())) {
            return CompilerError.instantiateError(CompilerError.IDENTIFIER_NOT_FOUND, var.getTokenLine(), 0);
        }

        mCurrentExpression.clear();
        error = analyseExpression();
        if (error.getErrorCode() != CompilerError.NONE_ERROR) return error;

        //[ANALISE SEMANTICA]
        int index = mSemantic.getFirstIndexOf(var.getLexema());
        try {
            int result = mSemantic.evaluateExpression(mCurrentExpression.toArray(new ExpressionElement[mCurrentExpression.size()]));
            int type = mSemantic.getVarFuncType(index);
            if (type == Symbols.SINTEIRO) {
                type = Semantic.EXPRESSION_EVALUATION_TYPE_INT;
            } else if (type == Symbols.SBOOLEANO) {
                type = Semantic.EXPRESSION_EVALUATION_TYPE_BOOLEAN;
            } else {
                throw new IllegalArgumentException("fodeu!");
            }

            // Verifica retorno de funcao
            if (mSemantic.isFunction(index) && !mSemantic.isScope(index)) {
                return CompilerError.instantiateError(CompilerError.FUNCTION_WRONG_ATTR, var.getTokenLine(), 0);
            }

            if (result != type && error.getErrorCode() == CompilerError.NONE_ERROR) {
                error = CompilerError.instantiateError(
                        CompilerError.EXPRESSION_BOOLEAN_EXPECTED,
                        mCurrentToken.getTokenLine(),
                        mCurrentToken.getTokenEndColumn());
            }
        } catch (InvalidExpressionException ex) {
            return CompilerError.instantiateError(CompilerError.EXPRESSION_INCOMPATIBLE_TYPES, mCurrentToken.getTokenLine(), 0);
        }
        SymbolTableEntry symbol = mSemantic.get(index);
        //[ANALISE SEMANTICA]

        //[GERACAO DE CODIGO]
        mCodeGenerator.appendCode("STR " + symbol.mAddress);
        //[GERACAO DE CODIGO]

        return error;
    }

    private CompilerError processProcCall(Token id) {
        CompilerError error = CompilerError.NONE();

        if (id == null || id.getSymbol() != Symbols.SIDENTIFICADOR) {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = id == null ? 0 : id.getTokenLine();
            int col  = id == null ? 0 : id.getTokenEndColumn();
            error = CompilerError.instantiateError(CompilerError.INVALID_PROC_FUNC_NAME, line, col);
        }
        //[ANALISE SEMANTICA]
        int index = mSemantic.getFirstIndexOf(id.getLexema());
        if (index != -1) {
            SymbolTableEntry symbol = mSemantic.get(index);
            if (symbol.mType == SymbolTableEntry.TYPE_PROCEDURE) {
                //[GERACAO DE CODIGO]
                mCodeGenerator.appendCode("CALL " + symbol.mLabel);
                //[GERACAO DE CODIGO]
            } else {
                error = CompilerError.instantiateError(CompilerError.IDENTIFIER_NOT_FOUND,
                        mCurrentToken.getTokenLine(), 0);
            }
        } else {
            error = CompilerError.instantiateError(CompilerError.IDENTIFIER_NOT_FOUND,
                    mCurrentToken.getTokenLine(), 0);
        }
        //[ANALISE SEMANTICA]

        return error;
    }

    private CompilerError processFuncCall() {
        CompilerError error = CompilerError.NONE();

        if (mCurrentToken == null || mCurrentToken.getSymbol() != Symbols.SIDENTIFICADOR) {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            return CompilerError.instantiateError(CompilerError.INVALID_PROC_FUNC_NAME, line, col);
        }
        //[ANALISE SEMANTICA]
        int index = mSemantic.getFirstIndexOf(mCurrentToken.getLexema());
        if (index != -1) {
            SymbolTableEntry symbol = mSemantic.get(index);
            if (symbol.mType == SymbolTableEntry.TYPE_FUNCTION) {
                //[GERACAO DE CODIGO]
                // Codigo chamado na hora de avaliar a expressao
                // ja que este metodo so e chamado em expressoes
                //[GERACAO DE CODIGO]
            } else {
                error = CompilerError.instantiateError(CompilerError.IDENTIFIER_NOT_FOUND,
                        mCurrentToken.getTokenLine(), 0);
            }
        } else {
            error = CompilerError.instantiateError(CompilerError.IDENTIFIER_NOT_FOUND,
                    mCurrentToken.getTokenLine(), 0);
        }
        //[ANALISE SEMANTICA]

        return error;
    }

    private CompilerError processIf() {
        CompilerError error = CompilerError.NONE();
        int lelse, lend;
        lend = mLabel;
        mLabel++;
        int jmpIndex;

        mCurrentToken = mTokenList.getTokenFromBuffer();
        mCurrentExpression.clear();
        error = analyseExpression();
        // O resultado da expressao deve ser booleano
        //[ANALISE SEMANTICA]
        try {
            int result = mSemantic.evaluateExpression(mCurrentExpression.toArray(new ExpressionElement[mCurrentExpression.size()]));
            if (result != Semantic.EXPRESSION_EVALUATION_TYPE_BOOLEAN
                    && error.getErrorCode() == CompilerError.NONE_ERROR) {
                error = CompilerError.instantiateError(
                        CompilerError.EXPRESSION_BOOLEAN_EXPECTED,
                        mCurrentToken.getTokenLine(),
                        mCurrentToken.getTokenEndColumn());
            }
        } catch (InvalidExpressionException ex) {
            return CompilerError.instantiateError(CompilerError.EXPRESSION_INCOMPATIBLE_TYPES, mCurrentToken.getTokenLine(), 0);
        }
        //[ANALISE SEMANTICA]
        // Nao continua se a analise da expressao ja falhou!
        if (error.getErrorCode() != CompilerError.NONE_ERROR) return error;

        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SENTAO) {
            lelse = mLabel;
            //[GERACAO DE CODIGO]
            jmpIndex = mCodeGenerator.appendCode("JMPF " + lelse); // ELSE
            //[GERACAO DE CODIGO]
            mLabel++;

            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = processSimpleCommand();
            //[GERACAO DE CODIGO]
            mCodeGenerator.appendCode("JMP " + lend); // Skip do else
            //[GERACAO DE CODIGO]
            if (mCurrentToken == null) {
                return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
            }
            if (error.getErrorCode() == CompilerError.NONE_ERROR &&
                    mCurrentToken.getSymbol() == Symbols.SSENAO) {
                //[GERACAO DE CODIGO]
                mCodeGenerator.appendCode(lelse + " NULL"); // else
                //[GERACAO DE CODIGO]

                mCurrentToken = mTokenList.getTokenFromBuffer();
                error = processSimpleCommand();
            } else {
                // Nao tem else muda o jmpf do if
                mCodeGenerator.modifyCode(jmpIndex, "JMPF " + lend);
            }
        } else {
            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
            int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
            error = CompilerError.instantiateError(CompilerError.MALFORMED_IF_CONSTRUCTION, line, col);
        }
        //[GERACAO DE CODIGO]
        mCodeGenerator.appendCode(lend + " NULL"); // fim do if-else
        //[GERACAO DE CODIGO]

        return error;
    }

    private CompilerError analyseExpression() {
        CompilerError error = CompilerError.NONE();

        error = analyseSimpleExpression();
        if (error.getErrorCode() != CompilerError.NONE_ERROR) return error;
        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
        }

        if (error.getErrorCode() == CompilerError.NONE_ERROR
                && (mCurrentToken.getSymbol() == Symbols.SMAIOR
                        || mCurrentToken.getSymbol() == Symbols.SMAIORIG
                        || mCurrentToken.getSymbol() == Symbols.SIG
                        || mCurrentToken.getSymbol() == Symbols.SMENOR
                        || mCurrentToken.getSymbol() == Symbols.SMENORIG || mCurrentToken
                        .getSymbol() == Symbols.SDIF)) {
            saveExpressionElement(mCurrentToken);
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseSimpleExpression();
        }
        return error;
    }

    private CompilerError analyseSimpleExpression() {
        CompilerError error = CompilerError.NONE();
        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
        }

        if (mCurrentToken.getSymbol() == Symbols.SMAIS || mCurrentToken.getSymbol() == Symbols.SMENOS) {
            saveExpressionElement(mCurrentToken, true);
            mCurrentToken = mTokenList.getTokenFromBuffer();
        }

        error = analyseTerm();
        if (error.getErrorCode() != CompilerError.NONE_ERROR) return error;
        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
        }

        while (mCurrentToken.getSymbol() == Symbols.SMAIS || mCurrentToken.getSymbol() == Symbols.SMENOS
                || mCurrentToken.getSymbol() == Symbols.SOU) {
            saveExpressionElement(mCurrentToken);
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseTerm();
            if (mCurrentToken == null) {
                // Token null - erro
                error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
            }
            if (error.getErrorCode() != CompilerError.NONE_ERROR) break;
        }
        return error;
    }

    private CompilerError analyseTerm() {
        CompilerError error = CompilerError.NONE();

        error = analyseFactor();
        if(error.getErrorCode() != CompilerError.NONE_ERROR) return error;
        if(mCurrentToken == null) return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);

        while (mCurrentToken.getSymbol() == Symbols.SMULT || mCurrentToken.getSymbol() == Symbols.SDIV
                || mCurrentToken.getSymbol() == Symbols.SE) {
            saveExpressionElement(mCurrentToken);
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseFactor();
            if (error.getErrorCode() != CompilerError.NONE_ERROR) return error;
            if (mCurrentToken == null) {
                error = CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
                break;
            }
        }
        return error;
    }

    private CompilerError analyseFactor() {
        CompilerError error = CompilerError.NONE();
        if (mCurrentToken == null) {
            // Token null - erro
            return CompilerError.instantiateError(CompilerError.UNKNOWN_ERROR, 0, 0);
        }

        if(mCurrentToken.getSymbol() == Symbols.SIDENTIFICADOR) {
            int index = mSemantic.getFirstIndexOf(mCurrentToken.getLexema());
            if(index != -1) {
                if(mSemantic.isFunction(index)) {
                    saveExpressionElement(mCurrentToken);
                    error = processFuncCall();
                    if(error.getErrorCode() != CompilerError.NONE_ERROR) return error;
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                } else {
                    saveExpressionElement(mCurrentToken);
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                }
            } else {
                error = CompilerError.instantiateError(CompilerError.IDENTIFIER_NOT_FOUND,
                        mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
            }
        } else if(mCurrentToken.getSymbol() == Symbols.SNUMERO) {
            saveExpressionElement(mCurrentToken);
            mCurrentToken = mTokenList.getTokenFromBuffer();
        } else if(mCurrentToken.getSymbol() == Symbols.SNAO) {
            saveExpressionElement(mCurrentToken);
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseFactor();
        } else if(mCurrentToken.getSymbol() == Symbols.SABRE_PARENTESES) {
            saveExpressionElement(mCurrentToken);
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = analyseExpression();
            // Se falhou, nao continua
            if(error.getErrorCode() != CompilerError.NONE_ERROR) return error;

            if(mCurrentToken.getSymbol() == Symbols.SFECHA_PARENTESES) {
                saveExpressionElement(mCurrentToken);
                mCurrentToken = mTokenList.getTokenFromBuffer();
            } else {
                error = CompilerError.instantiateError(CompilerError.MALFORMED_EXPRESSION,
                        mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
            }
        } else if (mCurrentToken.getSymbol() == Symbols.SVERDADEIRO ||
                mCurrentToken.getSymbol() == Symbols.SFALSO) {
            saveExpressionElement(mCurrentToken);
            mCurrentToken = mTokenList.getTokenFromBuffer();
        }

        return error;
    }

    private void saveExpressionElement(Token t) {
        saveExpressionElement(t, false);
    }

    private void saveExpressionElement(Token t, boolean isUn) {
        ExpressionElement e = new ExpressionElement(t.getLexema(), t.getSymbol());
        if (isUn) {
            e.mValue = e.mValue + "u";
        }
        mCurrentExpression.add(e);

    }

    private CompilerError processWhile() {
        CompilerError error = CompilerError.NONE();
        int label, label2;
        label = mLabel;
        //[GERACAO DE CODIGO]
        mCodeGenerator.appendCode(mLabel + " NULL"); // Inicio do while
        //[GERACAO DE CODIGO]
        mLabel++;

        mCurrentToken = mTokenList.getTokenFromBuffer();
        mCurrentExpression.clear();
        error = analyseExpression();
        // O resultado da expressao deve ser booleano
        //[ANALISE SEMANTICA]
        try {
            int result = mSemantic.evaluateExpression(mCurrentExpression.toArray(new ExpressionElement[mCurrentExpression.size()]));
            if (result != Semantic.EXPRESSION_EVALUATION_TYPE_BOOLEAN
                    && error.getErrorCode() == CompilerError.NONE_ERROR) {
                error = CompilerError.instantiateError(CompilerError.EXPRESSION_BOOLEAN_EXPECTED,
                        mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
            }
        } catch (InvalidExpressionException ex) {
            error = CompilerError.instantiateError(CompilerError.EXPRESSION_INCOMPATIBLE_TYPES, mCurrentToken.getTokenLine(), 0);
        }
        //[ANALISE SEMANTICA]
        // Nao continua se a analise da expressao ja falhou!
        if (error.getErrorCode() != CompilerError.NONE_ERROR) return error;

        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SFACA) {
            label2 = mLabel;
            //[GERACAO DE CODIGO]
            mCodeGenerator.appendCode("JMPF " + label2); // Skip do while se for falso
            //[GERACAO DE CODIGO]
            mLabel++;

            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = processSimpleCommand();
            //[GERACAO DE CODIGO]
            mCodeGenerator.appendCode("JMP " + label); // Volta ao inicio
            mCodeGenerator.appendCode(label2 + " NULL"); // Fim do while
            //[GERACAO DE CODIGO]
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
                int index = mSemantic.getFirstIndexOf(mCurrentToken.getLexema());
                if (index != -1) { //[ANALISE SEMANTICA]
                    SymbolTableEntry symbol = mSemantic.get(index);
                    if (symbol.mVarType == Symbols.SINTEIRO) { //[ANALISE SEMANTICA]
                        mCurrentToken = mTokenList.getTokenFromBuffer();
                        if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SFECHA_PARENTESES) {
                            mCurrentToken = mTokenList.getTokenFromBuffer();
                            // [GERACAO DE CODIGO]
                            mCodeGenerator.appendCode("RD");
                            mCodeGenerator.appendCode("STR " + symbol.mAddress);
                            // [GERACAO DE CODIGO]
                        } else {
                            // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                            int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                            int col = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                            error = CompilerError.instantiateError(CompilerError.MISSING_CLOSE_PARENTHESIS,
                                    line, col);
                        }
                    } else {
                        //[ANALISE SEMANTICA]
                        error = CompilerError.instantiateError(CompilerError.WRONG_VAR_TYPE, mCurrentToken.getTokenLine(), 0);
                    }
                } else {
                    //[ANALISE SEMANTICA]
                    error = CompilerError.instantiateError(CompilerError.IDENTIFIER_NOT_FOUND,
                            mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
                    }
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
                int index = mSemantic.getFirstIndexOf(mCurrentToken.getLexema()); //[ANALISE SEMANTICA]
                if (index != -1) { //[ANALISE SEMANTICA]
                    SymbolTableEntry symbol = mSemantic.get(index);
                    if (symbol.mType == SymbolTableEntry.TYPE_FUNCTION) {
                        //[GERACAO DE CODIGO]
                        mCodeGenerator.appendCode("CALL " + symbol.mLabel);
                        mCodeGenerator.appendCode("LDV " + symbol.mAddress);
                        //[GERACAO DE CODIGO]
                    } else if (symbol.mType == SymbolTableEntry.TYPE_VARIABLE) {
                        //[GERACAO DE CODIGO]
                        mCodeGenerator.appendCode("LDV " + symbol.mAddress);
                        //[GERACAO DE CODIGO]
                    } else {
                        //[ANALISE SEMANTICA]
                        return CompilerError.instantiateError(CompilerError.WRONG_VAR_FUNC_TYPE,
                                mCurrentToken.getTokenLine(), 0);
                    }
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                    if (mCurrentToken != null && mCurrentToken.getSymbol() == Symbols.SFECHA_PARENTESES) {
                        mCurrentToken = mTokenList.getTokenFromBuffer();
                        //[GERACAO DE CODIGO]
                        mCodeGenerator.appendCode("PRN");
                        //[GERACAO DE CODIGO]
                    } else {
                        // Se o token for null, setamos a linha e a coluna como '0' para evitar NullPointerException
                        int line = mCurrentToken == null ? 0 : mCurrentToken.getTokenLine();
                        int col  = mCurrentToken == null ? 0 : mCurrentToken.getTokenEndColumn();
                        error = CompilerError.instantiateError(CompilerError.MISSING_CLOSE_PARENTHESIS,
                                line, col);
                    }
                } else {
                    error = CompilerError.instantiateError(CompilerError.IDENTIFIER_NOT_FOUND,
                            mCurrentToken.getTokenLine(), mCurrentToken.getTokenEndColumn());
                }
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
