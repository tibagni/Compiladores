package vm.app.gui;

import java.awt.BorderLayout;
import java.awt.Color;

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

@SuppressWarnings("serial")
public class DebuggerWindow extends JFrame {
    private JButton mRunBtn;
    private JTextArea mOutputTxt;
    private JTable mInstructionsTable;
    private JTable mStackTable;

    // Menus
    private JMenuBar mMenuBar;
    private JMenu mFileMenu;
    private JMenu mRunMenu;

    // Paineis
    private JPanel mStackPanel;
    private JPanel mContentPanel;
    private JPanel mOutputPanel;
    
    // Scrolls 
    private JScrollPane mStackScroll;
    private JScrollPane mContentScroll;

    // Models
    private MemoryTableModel mStackTableModel;
    private InstructionsTableModel mInstructionsModel;
    
    /**
     * Consttrutor padrao. Cria layout da janela.
     */
    public DebuggerWindow() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Máquina Virtual");
        createMenu();
        createWindowLayout();
        pack();
        setSize(800, 600);
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
        // Painel da pilha
        mStackPanel = new JPanel();
        mStackPanel.setBackground(Color.BLACK);
        mStackTableModel = new MemoryTableModel();
        mStackTable = new JTable(mStackTableModel);
        mStackScroll = new JScrollPane(mStackTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mStackPanel.add(mStackScroll);

        mStackPanel.validate();
        getContentPane().add(mStackPanel, BorderLayout.EAST);

        // Painel do codigo fonte
        mContentPanel = new JPanel();
        mInstructionsModel = new InstructionsTableModel();
        mInstructionsTable = new JTable(mInstructionsModel);
        mContentScroll = new JScrollPane(mInstructionsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mContentPanel.add(mContentScroll);

        mContentPanel.validate();
        getContentPane().add(mContentPanel, BorderLayout.WEST);
    }
}
