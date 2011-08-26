package vm.hardware;

public class StdOut {
    // Pode ser que senha modificada por mais de uma thread
    private static volatile StdOut sInstance;

    private StringBuilder mOutput = new StringBuilder("");

    private StdOut() {}
    
    public static StdOut getInstance() {
        if (sInstance == null) {
            sInstance = new StdOut();
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
