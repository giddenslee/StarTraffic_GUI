package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import modeling.packet.PacketCollector;
import modeling.packet.PacketInfo;

public class TrafficComparision extends JPanel{
	
	List<PacketInfo> original;
	List<PacketInfo> generated;
	double startTime;
	double endTime;
	
	public TrafficComparision(List<PacketInfo> originalPackets, List<PacketInfo> generatedPackets, double startTime, double endTime) {
		// TODO Auto-generated constructor stub
		this.original = originalPackets;
		this.generated = generatedPackets;
		this.endTime = endTime;
		this.startTime = startTime;
		packetLevelComparision();
	}
	
	void packetLevelComparision()
	{
		ArrayList<PacketInfo> originalUp = new ArrayList<PacketInfo>();
		ArrayList<PacketInfo> originalDl = new ArrayList<PacketInfo>();
		PacketCollector.split(original, originalUp, originalDl);
		
		XYSeries upSeriesO = PacketCollector.createChartSeries(originalUp,
				"Original Upload Traffic",startTime, endTime);
		XYSeries dlSeriesO = PacketCollector.createChartSeries(originalDl,
				"Original Download Traffic",startTime, endTime);

		JFreeChart upChartO = ChartFactory.createXYLineChart(
				"Original Upload Traffic", "Time", "Packet Length",
				new XYSeriesCollection(upSeriesO), PlotOrientation.VERTICAL,
				false, true, false);
		upChartO.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE);
		JFreeChart dlChartO = ChartFactory.createXYLineChart(
				"Original Download Traffic", "Time", "Packet Length",
				new XYSeriesCollection(dlSeriesO), PlotOrientation.VERTICAL,
				false, true, false);
		dlChartO.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE);


		ChartPanel upPanelO = new ChartPanel(upChartO);
		upPanelO.setPreferredSize(new Dimension(1150, 250));

		ChartPanel dlPanelO = new ChartPanel(dlChartO);
		dlPanelO.setPreferredSize(new Dimension(1150, 250));		
		
		ArrayList<PacketInfo> generatedUp = new ArrayList<PacketInfo>();
		ArrayList<PacketInfo> generatedDl = new ArrayList<PacketInfo>();
		PacketCollector.split(generated, generatedUp, generatedDl);
		
		XYSeries upSeriesG = PacketCollector.createChartSeries(generatedUp,
				"Generated Upload Traffic",startTime, endTime);
		XYSeries dlSeriesG = PacketCollector.createChartSeries(generatedDl,
				"Generated Download Traffic",startTime, endTime);

		JFreeChart upChartG = ChartFactory.createXYLineChart(
				"Generated Upload Traffic", "Time", "Packet Length",
				new XYSeriesCollection(upSeriesG), PlotOrientation.VERTICAL,
				false, true, false);
		JFreeChart dlChartG = ChartFactory.createXYLineChart(
				"Generated Download Traffic", "Time", "Packet Length",
				new XYSeriesCollection(dlSeriesG), PlotOrientation.VERTICAL,
				false, true, false);

		ChartPanel upPanelG = new ChartPanel(upChartG);
		upPanelG.setPreferredSize(new Dimension(1150, 250));

		ChartPanel dlPanelG = new ChartPanel(dlChartG);
		dlPanelG.setPreferredSize(new Dimension(1150, 250));
		
		this.add(upPanelO);
		this.add(upPanelG);
		this.add(dlPanelO);
		this.add(dlPanelG);
	}
}
