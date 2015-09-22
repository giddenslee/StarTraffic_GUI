package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import modeling.tools.TextInOut;

public class TrafficModelingResult extends JPanel {

	String inputFileName;
	String modelFileName;
	String tempFileName;

	private TrafficModelingResult() {

	}

	private void analyzeModel() {
		JFrame frame = new TrafficModelAnalyzer(modelFileName, tempFileName);
		frame.setTitle("����ģ��:" + new File(modelFileName).getName());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}

	private void viewModel() {
		ArrayList<String> modelText = null;

		try {
			modelText = TextInOut.readFile(modelFileName + ".formatted");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();
		if (modelText == null) {
			sb.append("��ȡģ���ļ�ʧ��!\n");
		} else {
			for (String str : modelText) {
				sb.append(str);
				sb.append('\n');
			}
		}

		JFrame frame = new JFrame("�鿴ģ��:" + new File(modelFileName).getName());
		frame.setPreferredSize(new Dimension(500, 800));
		frame.setSize(frame.getPreferredSize().width,
				frame.getPreferredSize().height);
		frame.setLocationRelativeTo(null);

		JTextArea text = new JTextArea();
		text.setText(sb.toString());
		JScrollPane scrollPane = new JScrollPane(text);
		
		text.setCaretPosition(0);
		scrollPane.getVerticalScrollBar().setValue(0);
		//scrollPane.validate();
		
		frame.add(scrollPane);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}

	public TrafficModelingResult(String inputFileName, String outputModelName,
			String tempFileName) {
		this.inputFileName = inputFileName;
		this.modelFileName = outputModelName;
		this.tempFileName = tempFileName;

		File input = new File(inputFileName);
		File output = new File(outputModelName);

		this.setLayout(null);

		JLabel lbl1 = new JLabel(input.getName());
		lbl1.setBounds(0, 0, 300, 30);
		this.add(lbl1, null);

		JLabel lbl2 = new JLabel(output.getName());
		lbl2.setBounds(350, 0, 600, 30);
		this.add(lbl2, null);

		JButton btn1 = new JButton("�鿴ģ��");
		btn1.setBounds(700, 0, 100, 30);
		btn1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				viewModel();
			}
		});
		this.add(btn1, null);

		JButton btn2 = new JButton("����ģ��");
		btn2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				analyzeModel();
			}
		});
		btn2.setBounds(850, 0, 100, 30);

		this.setPreferredSize(new Dimension(1150, 30));
		this.setBackground(Color.WHITE);
		this.add(btn2, null);
	}

	public static TrafficModelingResult getHeader() {
		TrafficModelingResult result = new TrafficModelingResult();
		JLabel lbl1 = new JLabel("���������ļ�");
		JLabel lbl2 = new JLabel("���ģ���ļ�");
		JLabel lbl3 = new JLabel("�鿴ģ��");
		JLabel lbl4 = new JLabel("����ģ��");

		lbl1.setBounds(0, 0, 300, 30);
		lbl2.setBounds(350, 0, 600, 30);
		lbl3.setBounds(700, 0, 100, 30);
		lbl4.setBounds(850, 0, 100, 30);

		lbl3.setHorizontalAlignment(SwingConstants.CENTER);
		lbl4.setHorizontalAlignment(SwingConstants.CENTER);

		result.setLayout(null);
		result.add(lbl1, null);
		result.add(lbl2, null);
		result.add(lbl3, null);
		result.add(lbl4, null);

		result.setPreferredSize(new Dimension(1150, 30));
		result.setBackground(Color.WHITE);
		return result;
	}
}
