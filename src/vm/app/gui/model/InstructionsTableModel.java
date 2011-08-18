package vm.app.gui.model;

import javax.swing.table.AbstractTableModel;

import vm.app.SourceLine;
import vm.hardware.Memory;

@SuppressWarnings("serial")
public class InstructionsTableModel extends AbstractTableModel {
    private static final int LINE        = 0;
    private static final int LABEL       = 1;
    private static final int INSTRUCTION = 2;
    private static final int ATT1        = 3;
    private static final int ATT2        = 4;
    private static final int COMMENT        = 5;
    
    
    private Memory mMemoryReference = Memory.getInstance();

    @Override
    public int getRowCount() {
        return mMemoryReference.getSourceLineCount();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SourceLine line = mMemoryReference.getSourceLine(rowIndex);
        
        if (line == null) {
            return null;
        }
        
        switch(columnIndex) {
            case LINE:        return line.mLineNumber;
            case LABEL:       return line.mLabel;
            case INSTRUCTION: return line.mInstruction;
            case ATT1:        return line.mAtt1;
            case ATT2:        return line.mAtt2;
            case COMMENT:     return line.mComment;
            default:          return "-";
        }
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case LINE:        return "Linha";
            case LABEL:       return "Rótulo";
            case INSTRUCTION: return "Instrução";
            case ATT1:        return "Atributo 1";
            case ATT2:        return "Atributo 2";
            case COMMENT:     return "Comentário";
            default:          return "-";
        }
    }
}
