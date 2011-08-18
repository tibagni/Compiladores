package vm.hardware;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vm.app.SourceLine;

public class FileLoader {
    public static void load(File f) {
        // Padrao pra remover espacos em excessoda string
        Pattern pattern = Pattern.compile("\\s{2,}");

        Scanner input = null;
        try {
            input = new Scanner(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (input != null) {
            int lineNumber = 0;
            while (input.hasNextLine()) {
                // Remove os espacos em excesso
                Matcher m = pattern.matcher(input.nextLine());
                String[] line = m.replaceAll(" ").trim().split(" ");
                SourceLine sourceLine = new SourceLine();

                // Nao processa linhas vazias
                if (line.length > 0) {
                    sourceLine.mLineNumber = ++lineNumber;
                    if (line.length == 1) {
                        // Se houver apenas um elemento na linha, este elemento
                        // e a instrucao
                        sourceLine.mInstruction = line[0];
                    } else {
                        try {
                            // Tenta converter o segundo elemento da lista em inteiro
                            // para verificar se e ou nao um atributo
                            Integer.parseInt(line[1]);
                            // Se executar a prtir daqui a conversao para inteiro funcionou
                            // O primeiro elemento e uma instrucao e o segundo um atributo
                            sourceLine.mInstruction = line[0];
                            sourceLine.mAtt1 = line[1];
                            
                            // Verifica o segundo atributo
                            if (line.length == 3) {
                                sourceLine.mAtt2 = line[2];
                            }
                        } catch (NumberFormatException e) {
                            // A conversao para inteiro falhou. O segundo elemento e a instrucao
                            // e o primeiro o label.
                            sourceLine.mLabel = line[0];
                            sourceLine.mInstruction = line[1];
                            
                            // Verifica atributos
                            if (line.length == 3) { // Apenas um atributo
                                sourceLine.mAtt1 = line[2];
                            } else if (line.length == 4) { // Dois atributos
                                sourceLine.mAtt2 = line[3];
                            }
                        }
                    }
                }
                // Adiciona linha de codigo na memoria do programa
                Memory.getInstance().addSourceLine(sourceLine);
            }
        }
    }
}
