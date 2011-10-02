package comp.app.alg;

import comp.app.Simbolos;
import comp.app.Token;
import comp.app.error.CompilerError;

public class Syntactic extends Algorithm {
    private Tokens mTokenList = Tokens.getInstance();
    private Token mCurrentToken;

    public CompilerError execute() {
        int label = 1;
        CompilerError error = CompilerError.NONE();
        
        // TODO verificar se Lexico esta rodando ou se ja falhou
        // antes de requisitar o primeiro token
        
        // O primeiro token esperado e o 'programa'
        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null || mCurrentToken.getSimbolo() != Simbolos.SPROGRAMA) {
            return CompilerError.instantiateError(CompilerError.INVALID_PROGRAM_START, 0, 0, this);
        }
        
        // Le proximo token se a inicializacao do codigo esta correta
        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null || mCurrentToken.getSimbolo() != Simbolos.SIDENTIFICADOR) {
            // Um identificador era esperado aqui
            return null; // TODO invalidProgramName
        }
        // Identificador encontrado!
        // TODO inserir na tabela de simbolos
        mCurrentToken = mTokenList.getTokenFromBuffer();
        if (mCurrentToken == null || mCurrentToken.getSimbolo() != Simbolos.SPONTO_VIRGULA) {
            return null; // TODO IllegalEndOfExpression
        }
        
        error = blockAnalyser();
        if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
            if (mCurrentToken == null
                    || mCurrentToken.getSimbolo() != Simbolos.SPONTO) {
                return null; // TODO illegalEndProgram
            }

            if (!mTokenList.isLexicalFinishedWithouError()) {
                return null; // TODO ErroLexico - UnknownError
            }
        }

        // Nao houveram erros, analisador sintatico OK
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
        if (mCurrentToken != null && mCurrentToken.getSimbolo() == Simbolos.SVAR) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            if (mCurrentToken != null && mCurrentToken.getSimbolo() == Simbolos.SIDENTIFICADOR) {
                while (mCurrentToken.getSimbolo() == Simbolos.SIDENTIFICADOR) {
                    error = processVariables();
                    if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
                        if (mCurrentToken != null && mCurrentToken.getSimbolo() == Simbolos.SPONTO_VIRGULA) {
                            mCurrentToken = mTokenList.getTokenFromBuffer();
                        } else {
                            // TODO erro IllegalEndExpression
                        }
                    }
                }
            } else {
                // TODO erro IllegalVarDeclaration
            }
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
            if (mCurrentToken != null && (mCurrentToken.getSimbolo() == Simbolos.SVIRGULA ||
                    mCurrentToken.getSimbolo() == Simbolos.SDOISPONTOS)) {
                if (mCurrentToken.getSimbolo() == Simbolos.SVIRGULA) {
                    mCurrentToken = mTokenList.getTokenFromBuffer();
                    if (mCurrentToken != null) {
                        if (mCurrentToken.getSimbolo() == Simbolos.SDOISPONTOS) {
                            // TODO error = seila; IllegalEndExpression
                        }
                    } else {
                        // TODO error= seila; erro lexico NULL. Unknown
                        break;
                    }
                }
            } else {
                // TODO error = IllegalVarDeclaration;
                break;
            }
            
        } while (mCurrentToken.getSimbolo() == Simbolos.SDOISPONTOS);
        if (error.getErrorCode() == CompilerError.NONE_ERROR_CODE) {
            mCurrentToken = mTokenList.getTokenFromBuffer();
            error = typeAnalyser();
        }

        return error;
    }

    private CompilerError typeAnalyser() {
        CompilerError error = CompilerError.NONE();
        if (mCurrentToken == null || mCurrentToken.getSimbolo() != Simbolos.SINTEIRO ||
                mCurrentToken.getSimbolo() != Simbolos.SBOOLEANO) {
            // TODO error = UNKNOWN_VAR_TYPE;
        } else {
            // TODO coloca tipo na tabela de simbolos
            mCurrentToken = mTokenList.getTokenFromBuffer();
        }
            
        return error;        
    }

    private CompilerError processSubRoutine() {
        CompilerError error = CompilerError.NONE();
        
        return error;
    }

    private CompilerError processCommands() {
        CompilerError error = CompilerError.NONE();

        return error;
    }
}
