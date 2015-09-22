package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 * 
 * 一个JTextField, JButton组合的路径选取工具 JTextFiled用于展示，编辑，记忆路径
 * JButton用于调用JFileChooser选取路径
 *
 */

public class PathField extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	boolean fileChoosable;
	boolean dirChoosable;
	FileFilter fileFilter;
	JFileChooser fileChooser;
	JTextField textField;
	JButton button;

	public PathField() {
		this(true, true, null);
	}

	public PathField(boolean fileChoosable, boolean dirChoosable,
			FileFilter fileFilter) {
		this.fileChoosable = fileChoosable;
		this.dirChoosable = dirChoosable;
		this.fileFilter = fileFilter;
		textField = new JTextField();
		button = new JButton("…");
		button.setPreferredSize(new Dimension(40, 100));

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JFileChooser fileChooser = getFileChooser();
				File file = new File(getPath());

				if (file.exists()) {
					if (file.isDirectory())
						fileChooser.setCurrentDirectory(file);
					else
						fileChooser.setCurrentDirectory(file.getParentFile());
				}
				
				if(JFileChooser.APPROVE_OPTION == fileChooser.showDialog(new JLabel(), "选择文件")){
					file = fileChooser.getSelectedFile();
					if (file != null) {
						try {
							setPath(file.getCanonicalPath());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		});

		this.setLayout(new BorderLayout());
		this.add(textField, BorderLayout.CENTER);
		this.add(button, BorderLayout.EAST);
	}

	public void setPath(String path) {
		textField.setText(path);
	}

	public String getPath() {
		String path = textField.getText();
		if (path == null || path.isEmpty())
			return ".\\";
		return path;
	}

	private JFileChooser getFileChooser() {
		if (fileChooser != null)
			return fileChooser;

		fileChooser = new JFileChooser();

		if (fileChoosable) {
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (dirChoosable)
				fileChooser
						.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		} else if (dirChoosable)
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (fileFilter != null)
			fileChooser.setFileFilter(fileFilter);

		return fileChooser;
	}
}
