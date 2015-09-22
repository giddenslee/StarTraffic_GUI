package GUI;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import modeling.model.traffic.TrafficModel;
import modeling.packet.PacketInfo;

public class TrafficModelEvaluation extends JPanel{
	List<PacketInfo> original;
	List<PacketInfo> generated;
	
	TrafficModel oriModel;
	TrafficModel genModel;
	
	boolean showed = false;
	
	public TrafficModelEvaluation(List<PacketInfo> originalPackets, List<PacketInfo> generatedPackets) {
		// TODO Auto-generated constructor stub
		this.original = originalPackets;
		this.generated = generatedPackets;
		
		showEvaluation();
	}
	
	public void showEvaluation()
	{
		
		oriModel = TrafficModel.createModel("packet", 10, 50);
		oriModel.buildModelFromPackets(original);
		genModel = TrafficModel.createModel("packet", 10, 50);
		genModel.buildModelFromPackets(generated);
		
		XYSeriesCollection oriCollection = oriModel.createChartDataset();
		XYSeriesCollection genCollection = genModel.createChartDataset();
		

		this.add(getChartPanel(oriCollection, genCollection, "upload_packet_size_model", "upload packet size comparision"));
		this.add(getChartPanel(oriCollection, genCollection, "download_packet_size_model", "download packet size comparision"));
		this.add(getChartPanel(oriCollection, genCollection, "upload_packet_interval_model", "upload packet interval comparision"));
		this.add(getChartPanel(oriCollection, genCollection, "download_packet_interval_model", "download packet interval comparision"));
		
	}
	
	private ChartPanel getChartPanel(XYSeriesCollection oriCollection, XYSeriesCollection genCollection, String key, String title)
	{
		XYSeriesCollection collection = new XYSeriesCollection();
		XYSeries series1 = oriCollection.getSeries(key);
		XYSeries series2 = genCollection.getSeries(key);
		series1.setKey("original traffic");
		series2.setKey("generated traffic");
		
		String xAxisLabel = "";
		if (key.contains("size"))
			xAxisLabel = "Size";
		else if (key.contains("interval"))
		{
			xAxisLabel = "log10(Interval)";
			series1 = reduceAxisX(series1);
			series2 = reduceAxisX(series2);
		}
		collection.addSeries(series1);
		collection.addSeries(series2);
			
		JFreeChart chart = ChartFactory.createXYLineChart(title,
				xAxisLabel, "CDF", collection, PlotOrientation.VERTICAL, true,
				true, false);

		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(380, 300));
		return panel;
	}
	
	public XYSeries reduceAxisX(XYSeries oldSeries)
	{
		XYSeries series = new XYSeries(oldSeries.getKey(), false);

		for (int j = 0; j < oldSeries.getItemCount(); ++j) {
			XYDataItem xyitem = oldSeries.getDataItem(j);
			if (xyitem.getXValue() >= 10)
				series.add(Math.log10(xyitem.getXValue()),
						xyitem.getYValue());
		}
		return series;
	}
}
