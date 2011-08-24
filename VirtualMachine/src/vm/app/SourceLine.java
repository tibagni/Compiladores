package vm.app;

/**
 * Classe que mantem cada linha de codigo estruturada
 * @author Tiago
 *
 */
public class SourceLine {
    public int mLineNumber;
    public String mLabel;
    public String mInstruction;
    public String mAtt1;
    public String mAtt2;
    public String mComment;

    @Override
    public String toString() {
        return ("#" + mLineNumber + ": " + mLabel + " " + mInstruction +
            " " + mAtt1 + " " + mAtt2 + " " + mComment).replaceAll("null", " ");
    }
}
