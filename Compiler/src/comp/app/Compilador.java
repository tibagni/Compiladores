package comp.app;

import java.io.File;
import java.io.IOException;

import comp.app.alg.Lexical;

public class Compilador {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String fileName = args[0];
        File f = new File(fileName);
        
        if (f != null && f.exists()) {
            try {
                new Lexical().analiseLexica(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
