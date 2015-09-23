package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.parser.Entity;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ExtensionFileFilter;

import modeling.model.traffic.TrafficModel;
import modeling.packet.PacketCollector;
import modeling.packet.PacketInfo;
import modeling.tools.TextInOut;

public class TrafficCombound extends JPanel {

	private static final long serialVersionUID = -8663325580222779633L;
	private PathField pathModel1;
	private PathField pathModel2;

	private JTextField txtTime;
	
	private PathField pathOutput;

	private JButton btnStart;

	private JPanel panShow;

	
	private List<PacketInfo> latestPackets;

	public TrafficCombound() {
		this.setLayout(null);

		JLabel lbl1 = new JLabel("输入路径1");
		lbl1.setBounds(220, 50, 100, 30);
		this.add(lbl1);

		pathModel1 = new PathField(true, false, new ExtensionFileFilter(
				"生成流量文件", "model"));
		pathModel1.setPath(Setting.getWorkspacePath() + "models");
		pathModel1.setBounds(400, 50, 450, 30);
		this.add(pathModel1);
		
		JLabel lbl2 = new JLabel("输入路径2");
		lbl2.setBounds(220, 100, 100, 30);
		this.add(lbl2);

		pathModel2 = new PathField(true, false, new ExtensionFileFilter(
				"生成流量文件", "model"));
		pathModel2.setPath(Setting.getWorkspacePath() + "models");
		pathModel2.setBounds(400, 100, 450, 30);
		this.add(pathModel2);

		JLabel lbl3 = new JLabel("输出路径");
		lbl3.setBounds(220,150,100,30);
		this.add(lbl3);
		
		pathOutput = new PathField(false,true,null);
		pathOutput.setPath(Setting.getWorkspacePath()+"generated");
		pathOutput.setBounds(400,150,450,30);
		this.add(pathOutput);
		
		JLabel lbl4 = new JLabel("生成时长");
		lbl4.setBounds(220,200,150,30);
		this.add(lbl4);
		
		txtTime = new JTextField("10");
		txtTime.setBounds(400,200,150,30);
		this.add(txtTime);
		
		JLabel lbl5 = new JLabel("秒");
		lbl5.setBounds(570,200,100,30);
		this.add(lbl5);

		panShow = new JPanel();
		panShow.setBounds(0, 240, 1200, 500);
		panShow.setBackground(Color.WHITE);
		this.add(panShow);
		 
		this.add(getStartButton(),null);
    }

	
	private void TrafficMix() {
		File input1 = new File(pathModel1.getPath());
		File input2 = new File(pathModel2.getPath());
		File output = new File(pathOutput.getPath());
		
		double endTime = 0;

		try {
			endTime = Double.parseDouble(txtTime.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "输入时间有误");
			return;
		}

		if (!input1.exists()) {
			JOptionPane.showMessageDialog(this, "输入模型文件1不存在");
			return;
		} else {
			if (!input1.getName().endsWith(".model")) {
				JOptionPane.showMessageDialog(this, "输入1不是一个模型文件");
				return;
			}
		}
		
		if (!input2.exists()) {
			JOptionPane.showMessageDialog(this, "输入模型文件2不存在");
			return;
		} else {
			if (!input2.getName().endsWith(".model")) {
				JOptionPane.showMessageDialog(this, "输入2不是一个模型文件");
				return;
			}
		}

		if (!output.exists() || !output.isDirectory()) {
			JOptionPane.showMessageDialog(this, "输出路径不存在");
			return;
		}

		ArrayList<String> modelText1,modelText2;
		TrafficModel model1 = null,model2 = null;
		try {
			modelText1 = TextInOut.readFile(input1.getAbsolutePath());
			model1 = TrafficModel.fromJson(modelText1.get(0));
			modelText2 = TextInOut.readFile(input2.getAbsolutePath());
			model2 = TrafficModel.fromJson(modelText2.get(0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ((model1 == null)||(model2 == null)) {
			JOptionPane.showMessageDialog(this, "读取模型文件失败");
			return;
		}
		List<PacketInfo> packets = model1.generatePackets(endTime);
		List<PacketInfo> packets2 = model2.generatePackets(endTime);
		packets.addAll(packets2);
		Collections.sort(packets);
		
		latestPackets = packets;
		ArrayList<PacketInfo> upPackets = new ArrayList<PacketInfo>();
		ArrayList<PacketInfo> dlPackets = new ArrayList<PacketInfo>();

		PacketCollector.split(packets, upPackets, dlPackets);
		
		try {
			String modelName1 = input1.getName();
			String modelName2 = input2.getName();
			modelName1 = modelName1.substring(0,
					modelName1.length() - ".model".length());
			modelName2 = modelName2.substring(0,
					modelName2.length() - ".model".length());
			String outputName = output.getAbsolutePath() + "\\" + modelName1+"&"+modelName2;

			PacketCollector.WriteToFile(packets, outputName + ".gen");
			PacketCollector.WriteToFile(dlPackets, outputName + ".dl.gen");
			PacketCollector.WriteToFile(upPackets, outputName + ".up.gen");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		showTraffic();
		
	}
	
	private void showTraffic() {
		panShow.removeAll();
		ArrayList<PacketInfo> upPackets = new ArrayList<PacketInfo>();
		ArrayList<PacketInfo> dlPackets = new ArrayList<PacketInfo>();
		PacketCollector.split(latestPackets, upPackets, dlPackets);

		double endTime = Double.parseDouble(txtTime.getText());
		XYSeries upSeries = PacketCollector.createChartSeries(upPackets,
				"Generated Upload Traffic", 0, endTime);
		XYSeries dlSeries = PacketCollector.createChartSeries(dlPackets,
				"Generated Download Traffic", 0, endTime);

		JFreeChart upChart = ChartFactory.createXYLineChart(
				"Generated Upload Traffic", "Time", "Packet Length",
				new XYSeriesCollection(upSeries), PlotOrientation.VERTICAL,
				false, true, false);
		JFreeChart dlChart = ChartFactory.createXYLineChart(
				"Generated Download Traffic", "Time", "Packet Length",
				new XYSeriesCollection(dlSeries), PlotOrientation.VERTICAL,
				false, true, false);

		ChartPanel upPanel = new ChartPanel(upChart);
		upPanel.setPreferredSize(new Dimension(1200, 250));

		ChartPanel dlPanel = new ChartPanel(dlChart);
		dlPanel.setPreferredSize(new Dimension(1200, 250));

		panShow.add(upPanel);
		panShow.add(dlPanel);

		panShow.validate();
	}
	private JButton getStartButton() {
		// TODO Auto-generated method stub
		if (btnStart == null) {
			btnStart = new JButton("开始生成");
			btnStart.setBounds(700, 200, 200, 30);
			btnStart.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					TrafficMix();
				}
			});
		}
		return btnStart;
	}
}
