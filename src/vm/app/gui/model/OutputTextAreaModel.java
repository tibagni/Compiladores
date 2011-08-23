package vm.app.gui.model;

import javax.swing.JTextArea;

import vm.hardware.OutputStream;

public class OutputTextAreaModel extends AbstractTextAreaModel {

    public OutputTextAreaModel(JTextArea component) {
        super(component);
    }

    @Override
    protected void onStructureChanged() {
        mComponent.setText(OutputStream.getInstance().getOutputString());
    }

    @Override
    protected void onReset() {
        OutputStream.getInstance().cleanOutputStream();
    }
}
