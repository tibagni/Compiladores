package vm.app.gui.model;

import javax.swing.table.AbstractTableModel;

import vm.hardware.Memory;

@SuppressWarnings("serial")
public class MemoryTableModel extends AbstractTableModel {
    private static final int ADDRESS = 0;
    private static final int VALUE   = 1;
    
    private Memory mMemoryReference = Memory.getInstance();

    @Override
    public int getRowCount() {
        return mMemoryReference.getTop() + 1;
    }

    @Override
    public int getColumnCount() {
        // Endereco e valor
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == ADDRESS) {
            return rowIndex;
        } else if (columnIndex == VALUE) {
            return mMemoryReference.getData(rowIndex);
        }
        return -1; // Evita NullPointerException
    }

    @Override
    public String getColumnName(int column) {
        if (column == ADDRESS) {
            return "Endereço";
        } else if (column == VALUE) {
            return "Valor";
        }
        return "-";
    }
}
