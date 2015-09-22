package GUI;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Setting extends JPanel {

	static String workspace = ".\\workspace\\";
	static Properties prop = new Properties();
	static File propFile = new File("traffic_gui.properties");

	public static void checkFistRun() {
		boolean workspaceSetted = false;

		if (propFile.isFile()) {
			try {
				FileInputStream fis = new FileInputStream(propFile);
				prop.load(fis);
				String workspacePath = prop.getProperty("workspace");
				if (workspacePath != null
						&& new File(workspacePath).isDirectory()) {
					workspace = workspacePath;
					mkdirs();
					workspaceSetted = true;
				}
				fis.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (workspaceSetted == false) {
			JDialog dialog = new JDialog();
			dialog.setTitle("设置workspace路径");
			dialog.setLocationRelativeTo(null);
			dialog.setPreferredSize(new Dimension(500, 200));
			dialog.setSize(500, 200);

			Setting setting = new Setting();
			setting.firstRunDialog = dialog;
			dialog.add(setting);

			dialog.setModal(true);
			dialog.setVisible(true);
		}
	}

	PathField pathField;
	JDialog firstRunDialog = null;
	JLabel lblMessage;

	public Setting() {
		this.setLayout(new FlowLayout());
		JLabel label = new JLabel("设置工作路径");
		label.setPreferredSize(new Dimension(100, 30));
		this.add(label);

		pathField = new PathField();
		pathField.setPath(workspace);
		pathField.setPreferredSize(new Dimension(400, 30));
		this.add(pathField);

		JButton button = new JButton("确定");
		button.setPreferredSize(new Dimension(100, 30));
		this.add(button);

		JLabel lblLine = new JLabel();
		lblLine.setPreferredSize(new Dimension(10000, 1));
		this.add(lblLine);
		
		lblMessage = new JLabel();
		lblMessage.setPreferredSize(new Dimension(400, 30));
		this.add(lblMessage);

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				workspace = pathField.getPath();
				mkdirs();
				if (new File(workspace).isDirectory()) {
					prop.setProperty("workspace", workspace);
					PrintWriter pw;
					try {
						pw = new PrintWriter(propFile);
						prop.list(pw);
						pw.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (firstRunDialog != null) {
						firstRunDialog.dispose();
					} else {
						lblMessage.setText("工作空间已设置，重启以切换工作空间");
					}
				} else {
					lblMessage.setText("非法路径");
				}
			}
		});

	}

	public static String getWorkspacePath() {
		if (workspace.endsWith("/") || workspace.endsWith("\\"))
			return workspace;
		else
			return workspace + "\\";
	}

	private static void mkdirs() {
		File file = new File(workspace);
		file.mkdirs();

		file = new File(workspace + "\\" + "classification\\.temp" + "\\");
		file.mkdirs();

		file = new File(workspace + "\\" + "fingerprint\\fingerprints" + "\\");
		file.mkdirs();

		file = new File(workspace + "\\" + "fingerprint\\.temp\\pcaptemp" + "\\");
		file.mkdirs();

		file = new File(workspace + "\\" + "fingerprint\\.temp\\txttemp" + "\\");
		file.mkdirs();
		
		file = new File(workspace + "\\" + "fingerprint\\.temp\\nametemp" + "\\");
		file.mkdirs();

		file = new File(workspace + "\\" + "traffics" + "\\");
		file.mkdirs();

		file = new File(workspace + "\\" + "models" + "\\");
		file.mkdirs();

		file = new File(workspace + "\\" + "generated" + "\\");
		file.mkdirs();
	}
}
