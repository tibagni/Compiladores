package vm.operation;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Thread para fila de operacoes em background (operacoes que nao
 * sao executadas na thread da UI)
 * @author Tiago
 *
 */
public class BackgroundOperation {
    // Fila de operacoes a serem executadas em background
    private static ArrayBlockingQueue<Runnable> sOperations = new ArrayBlockingQueue<Runnable>(10);
    private static Thread sOperationThread;

    /**
     * Inicia a thread que executa operacoes em background.
     * 
     * Esta thread mantem uma fila com todas as operacoes em background.
     * As operacoes sao executadas serialmente nesta thread (em paralelo com a thread da UI)
     */
    public static synchronized void startBackgroundOperationThread() {
        if (sOperationThread != null && sOperationThread.isAlive()) {
            // Thread ja esta rodando
            return;
        }
        sOperationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Runnable op = sOperations.take();
                        op.run();
                    } catch (InterruptedException e) { }
                }
            }
        }, "BackgroundOperationQueue");
        sOperationThread.start();
    }

    /**
     * Coloca um Runnable na fila para ser executado em background
     * Este metodo DEVE ser chamado somente pela thread da UI
     * 
     * @param operation Runnable a ser executado
     * @return true se a operacao foi enfileirada com sucesso ou false caso contrario
     */
    public static boolean runOnBackgroundThread(Runnable operation) {
        return sOperations.offer(operation);
    }
}
