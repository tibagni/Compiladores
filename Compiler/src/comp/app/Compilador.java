package comp.app;

import java.io.File;
import java.io.IOException;


import comp.app.alg.Lexical;
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
                new Lexical().analiseLexica(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
