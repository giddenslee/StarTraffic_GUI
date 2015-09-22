package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

public class TrafficGeneration extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8663325580222779633L;
	private PathField pathModel;
	private PathField pathOutput;
//pathfield
	private JTextField txtTime;

	private JButton btnStart;

	private JPanel panShow;

	private List<PacketInfo> latestPackets;

	public TrafficGeneration() {
		this.setLayout(null);

		JLabel lbl1 = new JLabel("流量模型");
		lbl1.setBounds(220, 50, 100, 30);
		this.add(lbl1);

		pathModel = new PathField(true, false, new ExtensionFileFilter(
				"流量模型文件", "model"));
		pathModel.setPath(Setting.getWorkspacePath() + "models");
		pathModel.setBounds(400, 50, 450, 30);
		this.add(pathModel);

		JLabel lbl2 = new JLabel("输出路径");
		lbl2.setBounds(220, 100, 100, 30);
		this.add(lbl2);

		pathOutput = new PathField(false, true, null);
		pathOutput.setPath(Setting.getWorkspacePath() + "generated");
		pathOutput.setBounds(400, 100, 450, 30);
		this.add(pathOutput);

		JLabel lbl3 = new JLabel("生成时长:");
		lbl3.setBounds(220, 150, 100, 30);
		this.add(lbl3);

		txtTime = new JTextField("10");
		txtTime.setBounds(400, 150, 150, 30);
		this.add(txtTime);

		JLabel lbl4 = new JLabel("秒");
		lbl4.setBounds(570, 150, 100, 30);
		this.add(lbl4);

		JButton button1 = new JButton("查看模型");
		button1.setBounds(900, 50, 150, 30);
		button1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				File file = new File(pathModel.getPath());
				if (file.isFile()) {
					TrafficModelingHistory history = TrafficModelingHistory
							.load(pathModel.getPath() + ".tag");
					if (history != null) {
						JFrame frame = new TrafficModelAnalyzer(
								history.modelFileName, history.tempFileName);
						frame.setTitle("分析模型:"
								+ new File(history.modelFileName).getName());
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						frame.setVisible(true);
					}
				}
			}
		});
		this.add(button1);

		JButton button2 = new JButton("对比原始流量");
		button2.setBounds(930, 160, 200, 50);
		button2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				showComparision();
			}
		});
		this.add(button2);

		panShow = new JPanel();
		panShow.setBounds(0, 240, 1200, 500);
		panShow.setBackground(Color.WHITE);
		this.add(panShow);

		this.add(getStartButton(), null);

	}

	private void generate() {
		File input = new File(pathModel.getPath());
		File output = new File(pathOutput.getPath());

		double endTime = 0;

		try {
			endTime = Double.parseDouble(txtTime.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "输入时间有误");
			return;
		}

		if (!input.exists()) {
			JOptionPane.showMessageDialog(this, "输入模型文件不存在");
			return;
		} else {
			if (!input.getName().endsWith(".model")) {
				JOptionPane.showMessageDialog(this, "输入不是一个模型文件");
				return;
			}
		}

		if (!output.exists() || !output.isDirectory()) {
			JOptionPane.showMessageDialog(this, "输出路径不存在");
			return;
		}

		ArrayList<String> modelText;
		TrafficModel model = null;
		try {
			modelText = TextInOut.readFile(input.getAbsolutePath());
			model = TrafficModel.fromJson(modelText.get(0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (model == null) {
			JOptionPane.showMessageDialog(this, "读取模型文件失败");
			return;
		}

		panShow.removeAll();

		List<PacketInfo> packets = model.generatePackets(endTime);
		latestPackets = packets;
		ArrayList<PacketInfo> upPackets = new ArrayList<PacketInfo>();
		ArrayList<PacketInfo> dlPackets = new ArrayList<PacketInfo>();

		PacketCollector.split(packets, upPackets, dlPackets);

		try {
			String modelName = input.getName();
			modelName = modelName.substring(0,
					modelName.length() - ".model".length());
			String outputName = output.getAbsolutePath() + "\\" + modelName;

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

	private void showComparision() {
		if (latestPackets == null)
			return;
		List<PacketInfo> originalPackets = null;
		List<PacketInfo> generatedPackets = latestPackets;

		File file = new File(pathModel.getPath());
		if (file.isFile()) {
			TrafficModelingHistory history = TrafficModelingHistory
					.load(pathModel.getPath() + ".tag");
			if (history != null) {
				PacketCollector collector = new PacketCollector();
				try {
					originalPackets = collector.collect(history.tempFileName);
					if (originalPackets != null) {
						double startTime = originalPackets.get(0).time;
						for (PacketInfo packet : originalPackets) {
							packet.time -= startTime;
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				double endTime1 = (originalPackets != null && originalPackets
						.size() > 0) ? originalPackets.get(
						originalPackets.size() - 1).getTimeSecond() : 0;
				double endTime2 = (generatedPackets != null && generatedPackets
						.size() > 0) ? generatedPackets.get(
						generatedPackets.size() - 1).getTimeSecond() : 0;

				JPanel comparisonPanel = new TrafficComparision(
						originalPackets, generatedPackets, 0, Math.max(
								endTime1, endTime2) + 0.1);
				comparisonPanel.setPreferredSize(new Dimension(1150, 1200));
				
				JPanel evaluationPanel = new TrafficModelEvaluation(originalPackets, generatedPackets);
				evaluationPanel.setPreferredSize(new Dimension(1150,1200));

				JFrame frame = new JFrame();
				frame.setSize(1200, 800);
				JTabbedPane tabbedPane = new JTabbedPane();
				tabbedPane.add("流量对比", new JScrollPane(comparisonPanel));
				tabbedPane.add("流量CDF对比", new JScrollPane(evaluationPanel));
				frame.add(tabbedPane);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setVisible(true);
			}
		}
	}

	private JButton getStartButton() {
		// TODO Auto-generated method stub
		if (btnStart == null) {
			btnStart = new JButton("开始生成");
			btnStart.setBounds(700, 160, 200, 50);
			btnStart.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					generate();
				}
			});
		}
		return btnStart;
	}
}
