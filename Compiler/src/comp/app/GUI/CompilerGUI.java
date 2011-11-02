package comp.app.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;

import comp.app.Compiler;
import comp.app.Compiler.UIListener;
import comp.app.error.CompilerError;
import comp.app.log.C_Log;
import comp.app.utils.Icons;
import comp.app.utils.MyFileChooser;

/**
 *
 * @author Felps
 * Classe responsável pela criação da interface gráfica do compilador
 */
public class CompilerGUI extends JFrame implements UIListener {

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
	private JFileChooser fileChooser = MyFileChooser.getFileChooser();

	/*
	 * Código Fonte
	 */
	private File sourceCodeFile = null;

	/*
	 * Padronização dos botões do JOptionPane
	 */
	private String[] opt = {"Sim", "Não"};

	/*
	 * JtextArea do codigo fonte
	 */
	private JTextArea sourceCodeArea;

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
	private JPanel sourceCodePanel;
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

	private static final String NEW_FILE_NAME = "NovoArquivo.lpd";
	private static final String LPD_FILE_EXTENSION = ".lpd";

	private boolean mIsCompiling = false;

	private int mLineCount = 1;

	public CompilerGUI() {
	    this(null);
	}

	public CompilerGUI(File f) {
        super(NEW_FILE_NAME);


        createMenu();
        createGUI();
        pack();
        config();

        if (f != null) {
            sourceCodeFile = f;
            setFile();
        }
	}

	private void createMenu() {
	    final Color bgColor = Color.WHITE;

		// Botão abrir
		open = new JButton(new ActionOpen());
		open.setBorderPainted(false);
		open.setIcon(Icons.getIcon(Icons.OPEN_ICON));
		open.setToolTipText("Abrir código fonte");

		// Botão novoArquivo
		newFile = new JButton(new ActionNewFile());
		newFile.setBorderPainted(false);
        newFile.setIcon(Icons.getIcon(Icons.NEW_ICON));
		newFile.setToolTipText("Criar novo códdigo fonte");

		// Botão salvar
		save = new JButton(new ActionSave());
		save.setBorderPainted(false);
        save.setIcon(Icons.getIcon(Icons.SAVE_ICON));
		save.setToolTipText("Salvar códdigo fonte");

		// Botão compilar
		compile = new JButton(new ActionCompile());
		compile.setBorderPainted(false);
        compile.setIcon(Icons.getIcon(Icons.COMPILE_ICON));
		compile.setToolTipText("Compilar código fonte");
		compile.setEnabled(false);

		// Label com nome do arquivo
		fileName = new JLabel(NEW_FILE_NAME);
		Border padding = BorderFactory.createEmptyBorder(0, 10, 0, 10);
		fileName.setBorder(padding);

		// MenuBar
		menu = new JMenuBar();

		menu.add(newFile);
		menu.add(open);
		menu.add(save);
		menu.add(compile);
		menu.add(fileName);

		menu.setBackground(bgColor);
        compile.setBackground(bgColor);
        save.setBackground(bgColor);
        newFile.setBackground(bgColor);
        open.setBackground(bgColor);

		setJMenuBar(menu);

	}

	private void createGUI() {

		/*
		 * Painel de escrita do codigo fonte e linhas ======================================
		 */
		sourceCodePanel = new JPanel();

		sourceCodeArea = new JTextArea(32,112);
        Border padding = BorderFactory.createEmptyBorder(0, 5, 0, 5);
		sourceCodeArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		sourceCodeArea.setBorder(padding);

		highlighter = sourceCodeArea.getHighlighter();

		scroll = new JScrollPane();

		lineNumber = new JTextArea("1");
		lineNumber.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		lineNumber.setBackground(Color.LIGHT_GRAY);
		lineNumber.setEditable(false);
		lineNumber.setBorder(padding);

		sourceCodeArea.getDocument().addDocumentListener(new DocumentListener() {

			public String getText() {
				int caretPosition = sourceCodeArea.getDocument().getLength();
				Element root = sourceCodeArea.getDocument().getDefaultRootElement();
				StringBuffer text = new StringBuffer("1" + System.getProperty("line.separator"));
				int elementIndex = root.getElementIndex( caretPosition );
				for(int i = 2; i < elementIndex + 2; i++){
					text.append(i + System.getProperty("line.separator"));
				}
				return text.toString();
			}
			@Override
			public void changedUpdate(DocumentEvent de) {
			    int currentLineCount = sourceCodeArea.getLineCount();
			    if (mLineCount != currentLineCount) {
			        mLineCount = currentLineCount;
			        lineNumber.setText(getText());
		            lineNumber.updateUI();
			    }
				compile.setEnabled(true);
				saved = false;
				highlighter.removeAllHighlights();
			}

			@Override
			public void insertUpdate(DocumentEvent de) {
                int currentLineCount = sourceCodeArea.getLineCount();
                if (mLineCount != currentLineCount) {
                    mLineCount = currentLineCount;
                    lineNumber.setText(getText());
                    lineNumber.updateUI();
                }
				compile.setEnabled(true);
				saved = false;
				highlighter.removeAllHighlights();
			}

			@Override
			public void removeUpdate(DocumentEvent de) {
                int currentLineCount = sourceCodeArea.getLineCount();
                if (mLineCount != currentLineCount) {
                    mLineCount = currentLineCount;
                    lineNumber.setText(getText());
                    lineNumber.updateUI();
                }
				compile.setEnabled(true);
				saved = false;
				highlighter.removeAllHighlights();
			}

		});
		scroll.getViewport().add(sourceCodeArea);
		scroll.setRowHeaderView(lineNumber);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		sourceCodePanel.add(scroll);
		sourceCodePanel.validate();
		getContentPane().add(sourceCodePanel, BorderLayout.CENTER);

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
		addWindowListener(new ActionClose());
		setLocationRelativeTo(null);
		setExtendedState(MAXIMIZED_BOTH);
	}

	/*
	 * Salva o arquivo fonte no lugar desejado
	 */
	private void salvarFonte() {

		/*
		 * Se fonteFile for null quer dizer que nao existe nenhum outro arquivo aberto
		 * entao o fileChooser é aberto para que se possa encolher onde salvar o arquivo
		 */
		if (sourceCodeFile == null) {
			fileChooser.setDialogTitle("Salvar");
			int resp = fileChooser.showSaveDialog(null);

			if(resp == MyFileChooser.CANCEL_OPTION) {
				salvarFonte();
			} else {
				// Crio o arquivo com o nome escolhido no fileChooser e adiciono a extensao caso necessario
				if(!fileChooser.getSelectedFile().getAbsolutePath().endsWith(LPD_FILE_EXTENSION)) {
					sourceCodeFile = new File(fileChooser.getSelectedFile().getAbsolutePath() + LPD_FILE_EXTENSION);
				} else {
					sourceCodeFile = new File(fileChooser.getSelectedFile().getAbsolutePath());
				}
				setTitle(sourceCodeFile.getName());
				fileName.setText(sourceCodeFile.getName());
				// Verifico se o arquivo já existe no diretório e pergunto se quer sobreescrevê-lo
				if(sourceCodeFile.exists()) {
					int res = JOptionPane.showOptionDialog(null,
							"Este arquivo já existe! Deseja sobreescrevê-lo?",
							"Atenção!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
							null, opt, opt[0]);

					if(res == JOptionPane.NO_OPTION) {
						sourceCodeFile = null;
						salvarFonte();
					}
				}
			}
		}

		// Escrevo no arquivo e seto arquivoSalvo
        FileOutputStream out = null;
		try{
		    out = new FileOutputStream(sourceCodeFile);
			out.write(sourceCodeArea.getText().getBytes());
			saved = true;
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (out != null) {
		    try {
                out.close();
            } catch (IOException e) {
                C_Log.error("Erro ao tentar fechar stream", e);
            }
		}
	}

	private class ActionClose extends WindowAdapter {
		public void windowClosing(WindowEvent e){
			if(!saved){
				int res = JOptionPane.showOptionDialog(null,
						"Deseja salvar o arquivo antes de sair?",
						"Atenção!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
						null, opt, opt[0]);

				if(res == JOptionPane.YES_OPTION) {
					salvarFonte();
				}
			}
		}
	}
	/*
	 * Classe provada que faz o action do botao Abrir
	 */
	private class ActionOpen extends AbstractAction implements ActionListener {

        private static final long serialVersionUID = 1L;

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
			sourceCodeArea.setText("");
			Scanner reader = null;

			fileChooser.setDialogTitle("Abrir");
			int resp = fileChooser.showOpenDialog(null);
			if(resp == JFileChooser.CANCEL_OPTION) {
				return;
			}
			sourceCodeFile = fileChooser.getSelectedFile();

			try {
				reader = new Scanner(sourceCodeFile);
			} catch (FileNotFoundException ex) {
				JOptionPane.showMessageDialog(null, "Erro", "Arquivo inexistente", JOptionPane.ERROR_MESSAGE);
				return;
			}

			reader.close();

			setFile();
		}
	}

	private void setFile() {
        if (sourceCodeFile != null) {
            Scanner fileReader;
            try {
                fileReader = new Scanner(sourceCodeFile);
            } catch (FileNotFoundException e) {
                C_Log.error("Arquivo nao encontrado", e);
                return;
            }
            while (fileReader.hasNextLine()) {
                sourceCodeArea.append(fileReader.nextLine() + "\n");
            }

            // Como o arquivo acabou de ser aberto, seto-o como salvo
            // Atualizo o label do nome do arquivo
            saved = true;
            fileName.setText(sourceCodeFile.getName());
            setTitle(sourceCodeFile.getName());
            errors.setText("");
            //sourceCodeArea.setCaretPosition(0);
            fileReader.close();
	    }
	}

	/*
	 * Classe responsavel pelo action do botao Compilar
	 */
	private class ActionCompile extends AbstractAction implements ActionListener {

        private static final long serialVersionUID = 1L;

        @Override
		public void actionPerformed(ActionEvent e) {
            // Nao tenta compilar se ja houver uma compilacão rodando
            if (mIsCompiling) {
                return;
            }

			// Salvo o fonte antes de compilar caso ele nao esteja salvo e faço a compilação
			if (!saved) {
				salvarFonte();
			}
			setCompilerRunning(true);

			compiler = new Compiler(sourceCodeFile, CompilerGUI.this);
			compiler.start();
		}
	}

	/*
	 * Classe responsável pelo action do botão novoArquivo
	 */
	private class ActionNewFile extends AbstractAction implements ActionListener {

        private static final long serialVersionUID = 1L;

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
			sourceCodeArea.setText("");
			fileName.setText(NEW_FILE_NAME);
			setTitle(NEW_FILE_NAME);
			sourceCodeFile = null;
			errors.setText("");
		}
	}

	/*
	 *  Classe responsável pelo action do botão Salvar
	 */
	private class ActionSave extends AbstractAction implements ActionListener {

        private static final long serialVersionUID = 1L;

        @Override
		public void actionPerformed(ActionEvent e) {
			salvarFonte();
		}
	}

	/*
	 * Quando ocorre um erro, a linha é sublinhada de azul
	 * para indicar onde o erro ocorreu
	 */
	private void setLineHighlighter(CompilerError erro) {

		try {
			int lineOffset;
			if(erro.getErrorCode() == CompilerError.INVALID_PROGRAM_START) {
				lineOffset = erro.getLineNumber();
			} else {
				lineOffset = erro.getLineNumber()-1;
			}

			highlighter.addHighlight(sourceCodeArea.getLineStartOffset(lineOffset),
					sourceCodeArea.getLineEndOffset(lineOffset),
					new MyHighlighter(Color.RED));

			sourceCodeArea.requestFocus();
			// Mudo a posição do cursor para a linha do erro.
			sourceCodeArea.setCaretPosition(sourceCodeArea.getLineEndOffset(lineOffset)-1);
		} catch (BadLocationException e) {
		    C_Log.error("Nao foi possivel sublinhar linha", e);
			return;
		}

	}

	private void setCompilerRunning(boolean isCompiling) {
	    if (isCompiling) {
	        compile.setIcon(Icons.getIcon(Icons.LOADING_ICON));
	        compile.setFocusable(false);
	    } else {
	        compile.setIcon(Icons.getIcon(Icons.COMPILE_ICON));
	        compile.setFocusable(true);
	    }
	    mIsCompiling = isCompiling;
	}

	/*
	 * Seta no textArea de erros qual erro aconteceu durante a copilação
	 */
	private void setErrorMessage(CompilerError error) {
		if(error.getErrorCode() == CompilerError.NONE_ERROR) {
			errors.setForeground(Color.GREEN);
		} else {
			setLineHighlighter(error);
			errors.setForeground(Color.RED);
		}
		errors.setText(error.getErrorMessage());
	}

	public static void main(String[] args) {
        final String fileName;
        final File file;

	    if (args.length > 0) {
	        fileName = args[0];
	        file = new File(fileName);
	    } else {
	        fileName = null;
	        file = null;
	    }
		// Antes de tudo
	    // Limpa todos os arquivos de log
		C_Log.clearLogFiles();

		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CompilerGUI ide;
                if (file != null) {
                    ide = new CompilerGUI(file);
                } else {
                    ide = new CompilerGUI();
                }
                ide.setVisible(true);
            }
        });
	}

	/*
	 * Classe utilizada para mudar a cor do highlighter
	 */
	private class MyHighlighter extends DefaultHighlighter.DefaultHighlightPainter {
		public MyHighlighter(Color c) {
			super(c);
		}
	}

    @Override
    public void onCompilationFinished(CompilerError result) {
        setErrorMessage(result);
        setCompilerRunning(false);
    }
}
