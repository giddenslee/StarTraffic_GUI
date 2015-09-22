package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import modeling.model.traffic.TrafficModel;
import modeling.packet.PacketCollector;
import modeling.packet.PacketInfo;
import modeling.tools.TextInOut;

public class TrafficModelAnalyzer extends JFrame {
	String modelFileName;
	String tempFileName;

	TrafficModel model;

	JPanel visualizationPanel;
	JPanel comparisonPanel;
	JPanel evaluationPanel;

	List<PacketInfo> originalPackets;
	List<PacketInfo> generatedPackets;

	public TrafficModelAnalyzer(String modelFileName, String tempFileName) {
		this.modelFileName = modelFileName;
		this.tempFileName = tempFileName;

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1200, 800));
		this.setSize(this.getPreferredSize().width,
				this.getPreferredSize().height);

		loadModel();

		visualizationPanel = new JPanel();
		visualizationPanel.setLayout(new FlowLayout());
		addModelChartPanels();

		comparisonPanel = makeComparisionPanel(); // there initializes
													// original/generatedPackets
		evaluationPanel = new TrafficModelEvaluation(originalPackets,
				generatedPackets);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("模型可视化", new JScrollPane(visualizationPanel));
		tabbedPane.add("流量对比", new JScrollPane(comparisonPanel));
		tabbedPane.add("模型评估", new JScrollPane(evaluationPanel));

		this.add(tabbedPane, BorderLayout.CENTER);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
				if (visualizationPanel != null)
					visualizationPanel.setPreferredSize(new Dimension(
							TrafficModelAnalyzer.this.getSize().width - 150,
							1500));
				if (evaluationPanel != null)
					evaluationPanel.setPreferredSize(new Dimension(
							TrafficModelAnalyzer.this.getSize().width - 150,
							1500));
			}
		});

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void loadModel() {
		try {
			ArrayList<String> modelText = TextInOut.readFile(modelFileName);
			model = TrafficModel.fromJson(modelText.get(0));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "读取模型文件失败");
			e.printStackTrace();
		}
	}

	private void addModelChartPanels() {
		XYSeriesCollection collection = model.createChartDataset();

		for (int i = 0; i < collection.getSeriesCount(); ++i) {
			XYSeriesCollection dataset = new XYSeriesCollection(
					collection.getSeries(i));

			String seriesName = (String) dataset.getSeries(0).getKey();
			String xAxisLabel = "";
			if (seriesName.contains("size"))
				xAxisLabel = "Size";
			if (seriesName.contains("interval")) {
				xAxisLabel = "log10(Interval)";
				XYSeries oldSeries = dataset.getSeries(0);
				XYSeries series = new XYSeries(oldSeries.getKey(), false);

				for (int j = 0; j < oldSeries.getItemCount(); ++j) {
					XYDataItem xyitem = oldSeries.getDataItem(j);
					if (xyitem.getXValue() >= 10)
						series.add(Math.log10(xyitem.getXValue()),
								xyitem.getYValue());
				}

				dataset = new XYSeriesCollection(series);
			}
			JFreeChart chart = ChartFactory.createXYLineChart(seriesName,
					xAxisLabel, "CDF", dataset, PlotOrientation.VERTICAL, true,
					true, false);

			ChartPanel panel = new ChartPanel(chart);
			panel.setPreferredSize(new Dimension(380, 300));

			visualizationPanel.add(panel);
		}
	}

	JPanel makeComparisionPanel() {
		PacketCollector collector = new PacketCollector();
		try {
			originalPackets = collector.collect(tempFileName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		double endTimeSecond = (originalPackets != null && originalPackets
				.size() > 0) ? originalPackets.get(originalPackets.size() - 1)
				.getTimeSecond() : 0;
		model.setStartTime(originalPackets.get(0).getTimeSecond());
		generatedPackets = model.generatePackets(endTimeSecond);
		comparisonPanel = new TrafficComparision(originalPackets,
				generatedPackets, originalPackets.get(0).getTimeSecond(),
				endTimeSecond + 0.1);
		comparisonPanel.setPreferredSize(new Dimension(1150, 1050));
		return comparisonPanel;
	}
}
