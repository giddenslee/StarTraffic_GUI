package GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;

import org.jfree.ui.ExtensionFileFilter;

import modeling.model.traffic.TrafficModel;
import modeling.model.traffic.TrafficModelSession;
import modeling.model.traffic.TrafficModelPacket;
import modeling.packet.PacketCollector;
import modeling.tools.PcapHelper;
import modeling.tools.TextInOut;

public class TrafficModeling extends JPanel {
	private JLabel lblPrecisionSize;
	private JTextField txtPrecisionSize;
	private JLabel lblPrecisionSizeTip;

	private JLabel lblPrecisionTime;
	private JTextField txtPrecisionTime;
	private JLabel lblPrecisionTimeTip;
	
	private PathField pathTraffic;
	private PathField pathOutput;

	private JRadioButton btnSession;
	private JRadioButton btnPacket;
	private ButtonGroup btnLevel;

	private JButton btnStart;
	
	private JPanel panResults;

	public TrafficModeling() {
		this.setLayout(null);
		
		//第一栏，流量文件/路径选择
		JLabel lbl1 = new JLabel("流量文件/路径");
		lbl1.setBounds(220, 50, 160, 30);
		this.add(lbl1);
		pathTraffic = new PathField(true, true, new ExtensionFileFilter("pcap流量文件", "pcap"));
		pathTraffic.setPath(Setting.getWorkspacePath()+"traffics");
		pathTraffic.setBounds(400, 50, 450, 30);
		this.add(pathTraffic);
		
		//第二栏，输出路径选择
		JLabel lbl2 = new JLabel("模型输出路径");
		lbl2.setBounds(220, 100, 160, 30);
		this.add(lbl2);
		pathOutput = new PathField(false, true, null);
		pathOutput.setPath(Setting.getWorkspacePath()+"models");
		pathOutput.setBounds(400,100,450,30);
		this.add(pathOutput);
		
		//第三栏，建模级别
		JLabel lbl3 = new JLabel("建模级别");
		lbl3.setBounds(220, 150, 100, 30);
		this.add(lbl3);
		this.add(getSessionbutton());
		this.add(getPacketbutton());
		bindButtonGroup();
		
		//第四栏，建模精度1
		lblPrecisionSize = new JLabel("Packet长度建模精度");
		lblPrecisionSize.setBounds(220, 200, 150, 30);
		this.add(lblPrecisionSize);
		
		txtPrecisionSize = new JTextField("10");
		txtPrecisionSize.setBounds(400, 200, 100, 30);
		this.add(txtPrecisionSize);

		lblPrecisionSizeTip = new JLabel("Bytes");
		lblPrecisionSizeTip.setBounds(510, 200, 400, 30);
		this.add(lblPrecisionSizeTip);

		//第五栏，建模精度2
		lblPrecisionTime = new JLabel("Packet时间间隔建模精度");
		lblPrecisionTime.setBounds(220, 250, 200, 30);
		this.add(lblPrecisionTime);

		txtPrecisionTime = new JTextField("50");
		txtPrecisionTime.setBounds(400, 250, 100, 30);
		this.add(txtPrecisionTime);

		lblPrecisionTimeTip = new JLabel("各数量级上的划分数(1~100值越大精度越高)");
		lblPrecisionTimeTip.setBounds(510, 250, 400, 30);
		this.add(lblPrecisionTimeTip);

		//开始建模按钮
		this.add(getStartButton());
		
		//结果展示部分
		JScrollPane scrollPane = new JScrollPane(getResultsPanel());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

//		for (int i=0;i<20;++i)
//			addResult("C:/123.pcap", "C:/466.model", "C:/46.tmp");
		
		scrollPane.setBounds(0, 300, 1280-100, 500-80);
		this.add(scrollPane);
	}

	// 开始建模
	private void start() {
		File input = new File(pathTraffic.getPath());
		File output = new File(pathOutput.getPath());

		String level = "packet";
		if (btnSession.isSelected())
			level = "session";

		int precisionSize = 20;
		int precisionTime = 50;

		ArrayList<String> inputFiles = new ArrayList<String>();

		try {
			precisionSize = Integer.parseInt(txtPrecisionSize.getText());
			precisionTime = Integer.parseInt(txtPrecisionTime.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "精度输入有误！");
			return;
		}

		if (!output.exists()||!output.isDirectory()) {
			JOptionPane.showMessageDialog(this, "输出路径不存在！");
			return;
		}

		if (input.exists()) {
			if (input.isDirectory()) {
				for (File file : input.listFiles()) {
					inputFiles.add(file.getAbsolutePath());
				}
			} else if (input.isFile()) {
				inputFiles.add(input.getAbsolutePath());
			}
		} else {
			JOptionPane.showMessageDialog(this, "输入路径/文件不存在！");
			return;
		}

		File temp = new File(output.getAbsolutePath() + "\\.temp");
		temp.mkdir();

		for (String pcapFile : inputFiles) {
			try {
				buildOneModel(pcapFile, output.getAbsolutePath(), level,
						precisionSize, precisionTime);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("使用" + pcapFile + "建模失败");
				e.printStackTrace();
			}
		}
		// temp.delete();
	}

	public void buildOneModel(String pcapFile, String outputDir, String level,
			int precisionSize, int precisionTime) throws Exception {
		File input = new File(pcapFile);
		String name = input.getName();

		if (!name.endsWith(".pcap")) {
			System.out.println(pcapFile + " is not a pcap file");
			return;
		}
		name = name.substring(0, name.length() - 5);
		String outputFilename = outputDir + "\\" + name + "_" + precisionSize
				+ "_" + precisionTime + "_" + level + ".model";

		String tempFile = outputDir + "\\.temp\\" + name + ".tmp";

		PcapHelper.packetSummarize(pcapFile, tempFile);

		PacketCollector collector = new PacketCollector();
		collector.collect(tempFile);

		TrafficModel model = TrafficModel.createModel(level, precisionSize,
				precisionTime);

		model.buildModelFromPackets(collector.getPacketList());

		TextInOut.writeFile(model.toString(), outputFilename);

		TextInOut.writeFile(model.toPrettyString(), outputFilename
				+ ".formatted");
		// new File(tempFile).delete();
		
		addResult(pcapFile, outputFilename, tempFile);
		TrafficModelingHistory history = new TrafficModelingHistory(pcapFile, tempFile, outputFilename);
		history.store(outputFilename+".tag");
	}
	
	private void addResult(String inputFileName, String outputModelName, String tempFileName)
	{
		TrafficModelingResult result = new TrafficModelingResult(inputFileName, outputModelName, tempFileName);
		panResults.add(result);
		panResults.setPreferredSize(new Dimension(1280, panResults.getComponentCount()*(30+5)));
		panResults.validate();
	}

	private JButton getStartButton() {
		// TODO Auto-generated method stub
		if (btnStart == null) {
			btnStart = new JButton("开始建模");
			btnStart.setBounds(900, 150, 200, 50);
			btnStart.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					start();
				}
			});
		}
		return btnStart;
	}
	
	private JPanel getResultsPanel()
	{
		if (panResults==null){
			panResults = new JPanel();
			panResults.setLayout(new FlowLayout());
			
			panResults.add(TrafficModelingResult.getHeader(), null);
			panResults.setBackground(Color.WHITE);
			panResults.setPreferredSize(new Dimension(1280, 35));
		}
		return panResults;
	}

	private void bindButtonGroup() {
		// TODO Auto-generated method stub
		if (btnLevel == null) {
			btnLevel = new ButtonGroup();

			btnLevel.add(btnSession);
			btnLevel.add(btnPacket);
		}
	}

	private JRadioButton getPacketbutton() {
		// TODO Auto-generated method stub
		if (btnPacket == null) {
			btnPacket = new JRadioButton("Packet", true);
			btnPacket.setBounds(400, 150, 100, 30);

			btnPacket.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					lblPrecisionSize.setText("Packet长度建模精度");
					txtPrecisionSize.setText("10");
					lblPrecisionSizeTip.setText("Bytes");

					lblPrecisionTime.setText("Packet时间间隔建模精度");
					txtPrecisionTime.setText("50");
					lblPrecisionTimeTip.setText("各数量级上的划分数(1~100值越大精度越高)");
				}
			});
		}
		return btnPacket;
	}

	private JRadioButton getSessionbutton() {
		// TODO Auto-generated method stub
		if (btnSession == null) {
			btnSession = new JRadioButton("Session");
			btnSession.setBounds(500, 150, 100, 30);

			btnSession.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					lblPrecisionSize.setText("Session长度建模精度");
					txtPrecisionSize.setText("50");
					lblPrecisionSizeTip.setText("各数量级上的划分数(1~100值越大精度越高)");

					lblPrecisionTime.setText("Session建立时间间隔精度");
					txtPrecisionTime.setText("50");
					lblPrecisionTimeTip.setText("各数量级上的划分数(1~100值越大精度越高)");
				}
			});
		}
		return btnSession;
	}
}
