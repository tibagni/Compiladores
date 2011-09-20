package comp.app;

import java.io.File;
import java.io.IOException;

import comp.app.alg.Lexical;
import comp.app.error.CompilerError;
import comp.app.log.C_Log;

public class Compilador {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String fileName = args[0];
        File f = new File(fileName);

        // Limpa todos os arquivos de log
        C_Log.clearLogFiles();
        
        if (f != null && f.exists()) {
            try {
                Lexical lexical = new Lexical();
                CompilerError error = lexical.analiseLexica(f);
                
                if (error.getErrorCode() != CompilerError.NONE_ERROR_CODE) {
                    System.out.println(error.getErrorMessage());
                } else {
                    System.out.println("Análise lexical Ok");                    
                }
                
            } catch (IOException e) {
                C_Log.error("Erro no arquivo!", e);
            }
        }
    }

}
