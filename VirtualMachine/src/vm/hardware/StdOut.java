package vm.hardware;

public class StdOut {
    private static StdOut sInstance;
    
    private static Object sInstanceLock = new Object();

    private StringBuilder mOutput = new StringBuilder("");

    private StdOut() {}
    
    public static StdOut getInstance() {
        // Temos que garantir que todas as threads tenham acesso a mesma
        // Instancia de stdOut. Nao pode ocorrer timeslice depois da comparacao
        // Senao corre o risoc de serem criadas duas instancias de stdOut
        // por duas Threads diferentes
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new StdOut();
            }
        }
        return sInstance;
    }

    public synchronized void appendOutput(String output) {
        mOutput.append(output + "\n");
    }

    public synchronized String getOutputString() {
        return mOutput.toString();
    }

    public synchronized void cleanOutputStream() {
        mOutput = null;
        mOutput = new StringBuilder("");
    }
}
