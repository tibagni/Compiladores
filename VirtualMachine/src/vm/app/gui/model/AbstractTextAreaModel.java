package vm.app.gui.model;

import javax.swing.JTextArea;

public abstract class AbstractTextAreaModel {
    protected JTextArea mComponent;

    public AbstractTextAreaModel(JTextArea component) {
        mComponent = component;
    }

    public final void fireTextAreaStructureChanged() {
        onStructureChanged();
    }

    public final void resetModel() {
        onReset();
        onStructureChanged();
    }

    protected abstract void onStructureChanged();
    protected abstract void onReset();
}
