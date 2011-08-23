package vm.hardware;

public class OutputStream {
    private static OutputStream sInstance;

    private StringBuilder mOutput = new StringBuilder("");

    private OutputStream() {}
    
    public static OutputStream getInstance() {
        if (sInstance == null) {
            sInstance = new OutputStream();
        }
        return sInstance;
    }

    public synchronized void appendOutput(String output) {
        mOutput.append(output + "\n");
    }

    public synchronized String getOutputString() {
        return mOutput.toString();
    }

    public void cleanOutputStream() {
        mOutput = null;
        mOutput = new StringBuilder("");
    }
}
