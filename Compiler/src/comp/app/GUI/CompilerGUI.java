package comp.app.GUI;

import comp.app.Compiler;
import comp.app.error.CompilerError;
import comp.app.log.C_Log;
import comp.app.utils.Icones;
import comp.app.utils.MyFileChooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;

/**
 *
 * @author Felps
 * Classe responsável pela criação da interface gráfica do compilador
 */
public class CompilerGUI extends JFrame {

	/**
	 * Não sei pra que diabos o eclipse manda colocar isso!!!
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * Highlighter para sublinhar a linha que houver algum erro
	 */
	private Highlighter highlighter;

	/*
	 * FileChooser customizado
	 */
	private JFileChooser fileChooser = MyFileChooser.getFileChooser();;

	/*
	 * Código Fonte
	 */
	private File fonteFile = null;

	/*
	 * Padronização dos botões do JOptionPane
	 */
	private String[] opt = {"Sim", "Não"};

	/*
	 * JtextArea do codigo fonte
	 */
	private JTextArea areaFonte;

	/*
	 * JTextArea utilizado para a contagem de linhas
	 */
	private JTextArea lineNumber;

	/*
	 * JTextArea de erros
	 */
	private JTextArea errors;

	/*
	 * Scrools para os textAreas do codigo fonte e erros
	 */
	private JScrollPane scroll;
	private JScrollPane scrollErrors;

	/*
	 * MenuBar contendo os botoes de novo, abrir, salvar e compilar
	 */
	private JMenuBar menu;
	private JButton open;
	private JButton compile;
	private JButton save;
	private JButton newFile;

	/*
	 * Paineis do codigo fonte e dos erros
	 */
	private JPanel painelFonte;
	private JPanel painelErros;

	/*
	 * Label com o nome do arquivo
	 */
	private JLabel fileName;

	/*
	 * Indica se o arquivo está ou não salvo
	 */
	private boolean saved = true;

	/*
	 * Objeto da classe Compilar, a qual processa a compilação
	 */
	private Compiler compiler;

	public CompilerGUI() {
		// TODO arrumar titulo e colocar uma imagem bunitinha =D
		super("Teste");
		createMenu();
		createGUI();
		pack();
		config();
	}

	private void createMenu() {

		// Botão abrir
		open = new JButton(new ActionOpen());
		open.setBorderPainted(false);
		open.setIcon(Icones.getIcon(Icones.OPEN_ICON));
		open.setToolTipText("Abrir código fonte");
		open.setBackground(Color.CYAN);

		// Botão novoArquivo
		newFile = new JButton(new ActionNewFile());
		newFile.setBorderPainted(false);
		newFile.setIcon(Icones.getIcon(Icones.NEW_ICON));
		newFile.setToolTipText("Criar novo códdigo fonte");
		newFile.setBackground(Color.CYAN);

		// Botão salvar
		save = new JButton(new ActionSave());
		save.setBorderPainted(false);
		save.setIcon(Icones.getIcon(Icones.SAVE_ICON));
		save.setToolTipText("Salvar códdigo fonte");
		save.setBackground(Color.CYAN);

		// Botão compilar
		compile = new JButton(new ActionCompile());
		compile.setBorderPainted(false);
		compile.setIcon(Icones.getIcon(Icones.COMPILE_ICON));
		compile.setToolTipText("Compilar código fonte");
		compile.setBackground(Color.CYAN);
		compile.setEnabled(false);

		// Label com nome do arquivo
		fileName = new JLabel("                    " + "Novo.txt");

		// MenuBar
		menu = new JMenuBar();
		menu.setBackground(Color.CYAN);

		menu.add(newFile);
		menu.add(open);
		menu.add(save);
		menu.add(compile);
		menu.add(fileName);

		setJMenuBar(menu);                

	}

	private void createGUI() {

		/*
		 * Painel de escrita do codigo fonte e linhas ======================================
		 */
		painelFonte = new JPanel();

		areaFonte = new JTextArea(32,112);        
		areaFonte.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

		highlighter = areaFonte.getHighlighter();

		scroll = new JScrollPane();

		lineNumber = new JTextArea("1");        
		lineNumber.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		lineNumber.setBackground(Color.LIGHT_GRAY);
		lineNumber.setEditable(false);

		areaFonte.getDocument().addDocumentListener(new DocumentListener(){
			public String getText(){
				int caretPosition = areaFonte.getDocument().getLength();
				Element root = areaFonte.getDocument().getDefaultRootElement();
				String text = "1" + System.getProperty("line.separator");
				for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
					text += i + System.getProperty("line.separator");
				}
				return text;
			}
			@Override
			public void changedUpdate(DocumentEvent de) {
				lineNumber.setText(getText());
				compile.setEnabled(true);
				saved = false;
				highlighter.removeAllHighlights();
			}

			@Override
			public void insertUpdate(DocumentEvent de) {
				lineNumber.setText(getText());
				compile.setEnabled(true);
				saved = false;
				highlighter.removeAllHighlights();
			}

			@Override
			public void removeUpdate(DocumentEvent de) {
				lineNumber.setText(getText());
				compile.setEnabled(true);
				saved = false;
				highlighter.removeAllHighlights();
			}

		});
		scroll.getViewport().add(areaFonte);
		scroll.setRowHeaderView(lineNumber);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		painelFonte.add(scroll);
		painelFonte.validate();
		getContentPane().add(painelFonte, BorderLayout.CENTER);

		/*
		 * Painel dos erros ====================================================================
		 */
		painelErros = new JPanel();

		errors = new JTextArea(5, 105);
		errors.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

		scrollErrors = new JScrollPane(errors, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		errors.setEditable(false);
		painelErros.add(scrollErrors);
		painelErros.setBorder(BorderFactory.createTitledBorder("Erros:"));
		painelErros.validate();
		getContentPane().add(painelErros, BorderLayout.SOUTH);
	}

	/*
	 * Configurações finais da interface gráfica
	 */
	private  void config() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setExtendedState(MAXIMIZED_BOTH);
		setVisible(true);
	}

	/*
	 * Salva o arquivo fonte no lugar desejado
	 */
	private void salvarFonte() {

		/*
		 * Se fonteFile for null quer dizer que nao existe nenhum outro arquivo aberto
		 * entao o fileChooser é aberto para que se possa encolher onde salvar o arquivo
		 */
		if (fonteFile == null) {
			fileChooser.setDialogTitle("Salvar");
			int resp = fileChooser.showSaveDialog(null);

			if(resp == MyFileChooser.CANCEL_OPTION) {
				return;
			} else {
				// Crio o arquivo com o nome escolhido no fileChooser e adiciono o .txt caso necessario
				if(!fileChooser.getSelectedFile().getAbsolutePath().endsWith(".txt")) {
					fonteFile = new File(fileChooser.getSelectedFile().getAbsolutePath() + ".txt");
				} else {
					fonteFile = new File(fileChooser.getSelectedFile().getAbsolutePath());
				}
				// Verifico se o arquivo já existe no diretório e pergunto se quer sobreescrevê-lo
				if(fonteFile.exists()) {
					int res = JOptionPane.showOptionDialog(null,
							"Este arquivo já existe! Deseja sobreescrevê-lo?",
							"Atenção!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
							null, opt, opt[0]);

					if(res == JOptionPane.NO_OPTION) {
						fonteFile = null;
						salvarFonte();
					}
				}
			}
		}

		// Escrevo no arquivo e seto arquivoSalvo
		try{
			FileOutputStream out = new FileOutputStream(fonteFile);
			out.write(areaFonte.getText().getBytes());
			saved = true;

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	/*
	 * Classe provada que faz o action do botao Abrir
	 */
	private class ActionOpen extends AbstractAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// Caso o arquivo não esteja salvo, pergunto antes de fecha-lo
			if(!saved) {
				int resp = JOptionPane.showOptionDialog(null, "Deseja salvar este arquivo antes de abrir outro?",
						"Arquivo não salvo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, opt,
						opt[0]);
				if(resp == JOptionPane.YES_OPTION) {
					salvarFonte();
				} else {
					saved = true;
				}
			}

			// Faço a leitura do arquivo
			areaFonte.setText("");
			Scanner reader = null;

			fileChooser.setDialogTitle("Abrir");
			int resp = fileChooser.showOpenDialog(null);
			if(resp == JFileChooser.CANCEL_OPTION) {
				return;
			}
			fonteFile = fileChooser.getSelectedFile();

			try {
				reader = new Scanner(fonteFile);

			} catch (FileNotFoundException ex) {
				JOptionPane.showMessageDialog(null, "Erro", "Arquivo inexistente", JOptionPane.ERROR_MESSAGE);
			}
			while(reader.hasNextLine()){
				areaFonte.append(reader.nextLine() + "\n");
			}

			// Como o arquivo acabou de ser aberto, seto-o como salvo
			// Atualizo o label do nome do arquivo
			saved = true;
			fileName.setText("                    " + fonteFile.getName());
			errors.setText("");
		}
	}

	/*
	 * Classe responsavel pelo action do botao Compilar
	 */
	private class ActionCompile extends AbstractAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// Salvo o fonte antes de compilar caso ele nao esteja salvo e faço a compilação
			CompilerError erro;
			if (!saved) {
				salvarFonte();
			}

			compiler = new Compiler(fonteFile);
			erro = compiler.compile();
			setErrorMessage(erro);
		}
	}

	/*
	 * Classe responsável pelo action do botão novoArquivo
	 */
	private class ActionNewFile extends AbstractAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			/*
			 * Caso haja outro arquivo aberto, pergunto se quer salvá-lo
			 */
			if(!saved) {
				int resp = JOptionPane.showOptionDialog(null, "Deseja salvar este arquivo antes de abrir outro?",
						"Arquivo não salvo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, opt,
						opt[0]);
				if(resp == JOptionPane.YES_OPTION) {
					salvarFonte();
				}
			}
			// Atualizo o label e seto o fonteFile para null para quando for salvar abrir
			// o fileChooser
			compile.setEnabled(false);
			areaFonte.setText("");
			fileName.setText("                    " + "Novo.txt");
			fonteFile = null;
			errors.setText("");
		}
	}

	/*
	 *  Classe responsável pelo action do botão Salvar
	 */
	private class ActionSave extends AbstractAction implements ActionListener {        

		@Override
		public void actionPerformed(ActionEvent e) {            
			salvarFonte();
		}
	}

	/*
	 * Quando ocorre um erro, a linha é sublinhada de azul
	 * para indicar onde o erro ocorreu
	 */
	public void setLineHighlighter(CompilerError erro) {

		try {
			int lineOffset = erro.getLineNumber()-1;

			highlighter.addHighlight(areaFonte.getLineStartOffset(lineOffset),
					areaFonte.getLineEndOffset(lineOffset), 
					new MyHighlighter(Color.RED));

			areaFonte.requestFocus();
			// Mudo a posição do cursor para a linha do erro.
			areaFonte.setCaretPosition(areaFonte.getLineEndOffset(lineOffset)-1);
		} catch (BadLocationException e) {
			return;
		}

	}

	/*
	 * Seta no textArea de erros qual erro aconteceu durante a copilação
	 */
	public void setErrorMessage(CompilerError erro) {
		if(erro.getErrorCode() == CompilerError.NONE_ERROR) {
			errors.setForeground(Color.GREEN);
		} else {
			setLineHighlighter(erro);
			errors.setForeground(Color.RED);
		}
		errors.setText(erro.getErrorMessage());
	}

	public static void main(String[] args) {
		//        String fileName = args[0];
		//        File f = new File(fileName);

		// Limpa todos os arquivos de log
		C_Log.clearLogFiles();

		new CompilerGUI();



		//        if (f != null && f.exists()) {
		//            new Compiler(f).compile();
		//        }
	}

	/*
	 * Classe utilizada para mudar a cor do highlighter
	 */
	public class MyHighlighter extends DefaultHighlighter.DefaultHighlightPainter {
		public MyHighlighter(Color c) {
			super(c);
		}
	}
}
