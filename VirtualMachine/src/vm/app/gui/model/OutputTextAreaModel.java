package vm.app.gui.model;

import javax.swing.JTextArea;

import vm.hardware.StdOut;

public class OutputTextAreaModel extends AbstractTextAreaModel {

    public OutputTextAreaModel(JTextArea component) {
        super(component);
    }

    @Override
    protected void onStructureChanged() {
        mComponent.setText(StdOut.getInstance().getOutputString());
    }

    @Override
    protected void onReset() {
        StdOut.getInstance().cleanOutputStream();
    }
}
