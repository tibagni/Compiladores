package vm.app;

import java.io.File;

import javax.swing.SwingUtilities;

import vm.app.gui.DebuggerWindow;
import vm.hardware.FileLoader;
import vm.hardware.Memory;
import vm.hardware.Processor;
import vm.operation.BackgroundOperation;

public class VirtualMachine {

    /**
     * @param args
     */
    public static void main(String[] args) {
        File f = new File(args[0]);
        FileLoader.load(f);
        
        Processor.proccessLine(Memory.getInstance().getSourceLine(0));
        // TODO chamar metodo fireStructureDataSetChanged do table model cada vez que os dados forem alterados
        // Cria a janela na thread de eventos EDT
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DebuggerWindow().setVisible(true);
            }
        });
        
        // Inicia a thread que ira executar as operacoes em background
        // Esta thread permanece bloqueada enquanto nao ha o que executar
        BackgroundOperation.startBackgroundOperationThread();
    }
}