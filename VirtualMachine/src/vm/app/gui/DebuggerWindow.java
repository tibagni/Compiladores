package vm.app.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import vm.app.gui.model.InstructionsTableModel;
import vm.app.gui.model.MemoryTableModel;
import vm.app.gui.model.OutputTextAreaModel;
import vm.hardware.FileLoader;
import vm.hardware.Memory;
import vm.hardware.Processor;
import vm.hardware.Processor.UiProcessorListener;
import vm.operation.BackgroundOperation;

@SuppressWarnings("serial")
public class DebuggerWindow extends JFrame implements UiProcessorListener {
    private static final short RUN_ALL_AT_ONCE  = 0;
    private static final short RUN_STEP_BY_STEP = 1;
    
    private JButton mRunBtn;
    private JTextArea mOutputTxt;
    private JTextArea mInputTxt;
    private JTable mInstructionsTable;
    private JTable mStackTable;
    private JFileChooser mFileChooser;
    private File mFile;
    
    // Menus
    private JMenuBar mMenuBar;
    private JMenu mFileMenu;
    private JMenu mRunMenu;
    private JMenuItem mOpenFile;
    private JRadioButtonMenuItem mStepByStepMode;
    private JRadioButtonMenuItem mRunAtOnceMode;

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

    private short mRunMode = RUN_ALL_AT_ONCE; // Modo padrao

    private static final Runnable sRunAllAtOnce = new Runnable() {
        @Override
        public void run() {
            while (!Processor.getInstance().proccessNextLine()) {
                // Nao faz nada
            }
        }
    };

    private static final Runnable sRunStepByStep = new Runnable() {
        @Override
        public void run() {
            Processor.getInstance().proccessNextLine();
        }
    };

    
    /**
     * Construtor padrao. Cria layout da janela.
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
        mOpenFile = new JMenuItem();
        mOpenFile.setText("Abrir Arquivo");
        mRunMenu = new JMenu();
        mRunMenu.setText("Executar");
        mRunAtOnceMode = new JRadioButtonMenuItem("Modo corrido");
        mRunAtOnceMode.setSelected(true);
        mStepByStepMode = new JRadioButtonMenuItem("Modo passo a passo");
        
        ButtonGroup runMenuGroup = new ButtonGroup();
        runMenuGroup.add(mRunAtOnceMode);
        runMenuGroup.add(mStepByStepMode);

        mMenuBar.add(mFileMenu);
        mMenuBar.add(mRunMenu);
        setJMenuBar(mMenuBar);
        mFileMenu.add(mOpenFile);
        mRunMenu.add(mRunAtOnceMode);
        mRunMenu.add(mStepByStepMode);

        mOpenFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Carrega o arquivo
                mFile = getFile();
                if (mFile != null) {
                    if (mFile != null) {
                        // Caso ja haja um arquivo rodando, array de source line e limpo
                        // e entao o novo arquivo e carregado.
                        Memory.getInstance().cleanSourceCode();
                        resetUi();
                    }
                    FileLoader.load(mFile);
                    mInstructionsModel.fireTableStructureChanged();
                }
            }
        });

        ActionListener runModeAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mRunAtOnceMode.isSelected()) {
                    mRunMode = RUN_ALL_AT_ONCE;
                } else if (mStepByStepMode.isSelected()) {
                    mRunMode = RUN_STEP_BY_STEP;
                }
            }
        };

        mRunAtOnceMode.addActionListener(runModeAction);
        mStepByStepMode.addActionListener(runModeAction);
    }

    /**
     * Pega o arquivo selecionado
     */
    private File getFile() {
        mFileChooser = new JFileChooser();
        mFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = mFileChooser.showOpenDialog(this);

        // Caso o usuario aperte cancel, o FileChooser e fechado.
        if (result == JFileChooser.CANCEL_OPTION) {
            mFileChooser.remove(this);
            return null;
        }

        return mFileChooser.getSelectedFile();
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
        mInstructionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
                disableRunOptionsMenu();
                // Executa as instrucoes na thread de background
                if (mRunMode == RUN_ALL_AT_ONCE) {
                    mRunBtn.setEnabled(false);
                    BackgroundOperation.runOnBackgroundThread(sRunAllAtOnce);
                } else if (mRunMode == RUN_STEP_BY_STEP) {
                    mRunBtn.setText("Próxima instrução");
                    BackgroundOperation.runOnBackgroundThread(sRunStepByStep);                    
                }
            }
        });
    }

    @Override
    public void onInstructionExecuted(int lineNumber) {
        mStackTableModel.fireTableStructureChanged();
        mInstructionsModel.fireTableStructureChanged();
        mOutputModel.fireTextAreaStructureChanged();

        // Muda o highlight da tabela para a instrucao atual
        mInstructionsTable.getSelectionModel().setSelectionInterval(0, lineNumber); // O primeiro valor nao importa
        
        // Muda o scroll da tela para acompanhar a execucao. Mas somente se for necessario
        int currentScrollValue = mInstructionsScroll.getVerticalScrollBar().getValue();
        int rowHeight = mInstructionsTable.getRowHeight();
        if (((currentScrollValue + lineNumber) * rowHeight) > 
        mInstructionsScroll.getVerticalScrollBar().getVisibleAmount()) { 
            // Move o scroll para ate 5 linhas antes para ter uma visao mais geral das instrucoes
            mInstructionsScroll.getVerticalScrollBar().setValue((lineNumber - 5) * rowHeight);
        }
    }

    @Override
    public void onInputEntered(String inputValue) {
        mInputTxt.append("Entrada: " + inputValue + "\n");
    }

    @Override
    public void onProgramFinished() {
        enableRunOptionsMenu();
        if (mRunMode == RUN_ALL_AT_ONCE) {
            mRunBtn.setEnabled(true);
        } else if (mRunMode == RUN_STEP_BY_STEP) {
            mRunBtn.setText("Rodar");                    
        }
    }

    @Override
    public void onRestartProgram() {
        resetUi();
    }

    private void resetUi() {          
        mInputTxt.setText("");
        mOutputModel.resetModel();
        mInstructionsModel.fireTableStructureChanged();        
    }

    private void disableRunOptionsMenu() {
        mRunMenu.setEnabled(false);
    }

    private void enableRunOptionsMenu() {
        mRunMenu.setEnabled(true);        
    }
}
