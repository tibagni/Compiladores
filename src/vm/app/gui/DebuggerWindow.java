package vm.app.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import vm.app.gui.model.InstructionsTableModel;
import vm.app.gui.model.MemoryTableModel;
import vm.app.gui.model.OutputTextAreaModel;
import vm.hardware.Processor;
import vm.hardware.Processor.UiProcessorListener;

@SuppressWarnings("serial")
public class DebuggerWindow extends JFrame implements UiProcessorListener {
    private JButton mRunBtn;
    private JTextArea mOutputTxt;
    private JTextArea mInputTxt;
    private JTable mInstructionsTable;
    private JTable mStackTable;

    // Menus
    private JMenuBar mMenuBar;
    private JMenu mFileMenu;
    private JMenu mRunMenu;

    // Paineis
    private JPanel mStackPanel;
    private JPanel mContentPanel;
    private JPanel mSouthPanel;
    private JPanel mButtonsPanel;
    private JPanel mOutputPanel;
    private JPanel mInputPanel;
    
    // Scrolls 
    private JScrollPane mStackScroll;
    private JScrollPane mInstructionsScroll;

    // Models
    private MemoryTableModel mStackTableModel;
    private InstructionsTableModel mInstructionsModel;
    private OutputTextAreaModel mOutputModel;
    
    /**
     * Consttrutor padrao. Cria layout da janela.
     */
    public DebuggerWindow() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Máquina Virtual");
        //setResizable(false);
        createMenu();
        createWindowLayout();
        pack();
        // Por fim seta listener para processador
        Processor.getInstance().setListener(this);
    }

    /**
     * Inicializa a bara de menus
     */
    private void createMenu() {
        mMenuBar = new JMenuBar();

        mFileMenu = new JMenu();
        mFileMenu.setText("Arquivo");
        mRunMenu = new JMenu();
        mRunMenu.setText("Executar");
        
        mMenuBar.add(mFileMenu);
        mMenuBar.add(mRunMenu);
        setJMenuBar(mMenuBar);
    }

    /**
     * Inicializa e posiciona os componentes na tela
     */
    private void createWindowLayout() {
        //****************************************** Painel da pilha
        mStackPanel = new JPanel();
        mStackTableModel = new MemoryTableModel();
        mStackTable = new JTable(mStackTableModel);
        mStackScroll = new JScrollPane(mStackTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Seta tamanho das colunas
        mStackTable.getColumnModel().getColumn(0).setMaxWidth(100); // COLUNA ENDERECO
        mStackTable.getColumnModel().getColumn(1).setMaxWidth(100); // COLUNA VALOR
        
        // Seta tamanho do scroll da pilha
        mStackScroll.setMinimumSize(new Dimension(202, 100));
        mStackScroll.setPreferredSize(new Dimension(202, 400));
        mStackScroll.setMaximumSize(new Dimension(50, 900));
        
        mStackPanel.add(mStackScroll);
        mStackPanel.setBorder(BorderFactory.createTitledBorder("Conteúdo da pilha"));

        mStackPanel.validate();
        getContentPane().add(mStackPanel, BorderLayout.EAST);

        
        //****************************************** Painel do codigo fonte
        mContentPanel = new JPanel();
        mInstructionsModel = new InstructionsTableModel();
        mInstructionsTable = new JTable(mInstructionsModel);
        mInstructionsScroll = new JScrollPane(mInstructionsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mContentPanel.add(mInstructionsScroll);
        mContentPanel.setBorder(BorderFactory.createTitledBorder("Código fonte - Intruções"));
        
        // Seta tamanho das colunas
        mInstructionsTable.getColumnModel().getColumn(0).setMinWidth(20); // COLUNA # LINHA
        mInstructionsTable.getColumnModel().getColumn(1).setMinWidth(20); // COLUNA LABEL
        mInstructionsTable.getColumnModel().getColumn(2).setMinWidth(80); // COLUNA INSTRUCAO
        mInstructionsTable.getColumnModel().getColumn(3).setMinWidth(70); // COLUNA ATT1
        mInstructionsTable.getColumnModel().getColumn(4).setMinWidth(70); // COLUNA ATT2
        mInstructionsTable.getColumnModel().getColumn(5).setMinWidth(150); // COLUNA COMENTARIO
        
        // Seta tamanho do scroll das instrucoes
        mInstructionsScroll.setMinimumSize(new Dimension(412, 100));
        mInstructionsScroll.setPreferredSize(new Dimension(500, 400));
        mInstructionsScroll.setMaximumSize(new Dimension(800, 900));

        mContentPanel.validate();
        getContentPane().add(mContentPanel, BorderLayout.CENTER);


        //****************************************** Painel inferior
        mSouthPanel = new JPanel(new BorderLayout());

        //**************** Painel de saida
        mOutputPanel = new JPanel();
        mOutputPanel.setBorder(BorderFactory.createTitledBorder("Saída"));
        mOutputTxt = new JTextArea(7, 20);
        mOutputModel = new OutputTextAreaModel(mOutputTxt);
        mOutputPanel.add(mOutputTxt);
        
        mSouthPanel.add(mOutputPanel, BorderLayout.EAST);

        //**************** Painel de entrada
        mInputPanel = new JPanel();
        mInputPanel.setBorder(BorderFactory.createTitledBorder("Entrada"));
        mInputTxt = new JTextArea(7, 20);
        mInputPanel.add(mInputTxt);
        
        mSouthPanel.add(mInputPanel, BorderLayout.WEST);

        //**************** Painel de botoes
        mButtonsPanel = new JPanel();
        mRunBtn = new JButton("Rodar");
        mButtonsPanel.add(mRunBtn);

        mButtonsPanel.setBorder(BorderFactory.createTitledBorder("Executar"));
        mSouthPanel.add(mButtonsPanel, BorderLayout.CENTER);

        mSouthPanel.validate();
        getContentPane().add(mSouthPanel, BorderLayout.SOUTH);
        
        mRunBtn.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO implementar modo um por vez e programa todo
                while (!Processor.getInstance().proccessNextLine()) {
                }
            }
        });
    }

    @Override
    public void onInstructionExecuted() {
        mStackTableModel.fireTableStructureChanged();
        mInstructionsModel.fireTableStructureChanged();
        mOutputModel.fireTextAreaStructureChanged();
    }

    @Override
    public void onInputEntered(String inputValue) {
        mInputTxt.append("Entrada: " + inputValue);
    }

    @Override
    public void onProgramFinished() {
        // TODO seila
    }

    @Override
    public void onRestartProgram() {
        mInputTxt.setText("");
        mOutputModel.resetModel();
        mInstructionsModel.fireTableStructureChanged();
    }
}
