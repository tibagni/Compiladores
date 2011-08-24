package vm.app;

import javax.swing.SwingUtilities;

import vm.app.gui.DebuggerWindow;
import vm.operation.BackgroundOperation;

public class VirtualMachine {

    /**
     * @param args
     */
    public static void main(String[] args) {
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