package modeling.model.traffic;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYDatasetTableModel;
import org.jfree.data.xy.XYSeriesCollection;

import modeling.model.distribution.Distribution;
import modeling.model.distribution.DistributionExponential;
import modeling.model.distribution.DistributionLinear;
import modeling.packet.PacketInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrafficModelPacket extends TrafficModel {

	@Expose
	@SerializedName("upload_packet_size_model")
	Distribution uploadPacketSize;
	@Expose
	@SerializedName("download_packet_size_model")
	Distribution downloadPacketSize;

	@Expose
	@SerializedName("upload_packet_interval_model")
	Distribution uploadInterval;
	@Expose
	@SerializedName("download_packet_interval_model")
	Distribution downloadInterval;

	public TrafficModelPacket() {
		this(100,100);
	}

	public TrafficModelPacket(double precisionSize, double precisionTime) {
		
		if (precisionSize<1)
			precisionSize = 1;
		if (precisionTime<1)
			precisionTime = 1;
		if (precisionTime>100)
			precisionTime = 100;
			
		uploadPacketSize = new DistributionLinear(precisionSize);
		downloadPacketSize = new DistributionLinear(precisionSize);
		uploadInterval = new DistributionExponential(precisionTime);
		downloadInterval = new DistributionExponential(precisionTime);

		reset();
	}

	@Override
	public void buildModelFromPackets(List<PacketInfo> packets) {

		double lastUploadTime = -1;
		double lastDownloadTime = -1;

		for (PacketInfo packet : packets) {
			if (packet.upload) {
				uploadPacketSize.addValue(packet.length);

				if (lastUploadTime != -1) {
					uploadInterval.addValue(packet.time - lastUploadTime);
				}
				lastUploadTime = packet.time;
			} else {
				downloadPacketSize.addValue(packet.length);
				if (lastDownloadTime != -1) {
					downloadInterval.addValue(packet.time - lastDownloadTime);
				}
				lastDownloadTime = packet.time;
			}
		}

		uploadPacketSize.buildModel();
		downloadPacketSize.buildModel();
		uploadInterval.buildModel();
		downloadInterval.buildModel();

		// System.out.println(uploadPacketSize);
	}

	double timeLine = 0;
	double nextUpload = -1.0;
	double nextDownload = -1.0;

	@Override
	public List<PacketInfo> generatePackets(double endTimeSecond) {
		double endTime = endTimeSecond * 1000000; // 以微秒表示时间

		if (nextUpload == -1.0) {
			nextUpload = timeLine + uploadInterval.predictValue();
			nextDownload = timeLine + Math.round((nextUpload - timeLine) / 2)
					+ downloadInterval.predictValue();
		}

		ArrayList<PacketInfo> result = new ArrayList<PacketInfo>();

		while (timeLine <= endTime) {
			if (nextUpload <= nextDownload) {
				timeLine = nextUpload;
				nextUpload += Math.round(uploadInterval.predictValue());

				long time = (long) timeLine;
				int length = (int) Math.round(uploadPacketSize.predictValue());
				PacketInfo packet = new PacketInfo(time, length, true);

				result.add(packet);
			} else {
				timeLine = nextDownload;
				nextDownload += Math.round(downloadInterval.predictValue());

				long time = (long) timeLine;
				int length = (int) Math
						.round(downloadPacketSize.predictValue());
				PacketInfo packet = new PacketInfo(time, length, false);

				result.add(packet);
			}
		}

		return result;
	}

	@Override
	public void reset() {
		timeLine = 0;
		nextUpload = nextDownload = -1.0;
	}

	@Override
	public int getParameterNumber() {
		// TODO Auto-generated method stub
		return uploadInterval.getParameterNumber()
				+ uploadPacketSize.getParameterNumber()
				+ downloadInterval.getParameterNumber()
				+ downloadPacketSize.getParameterNumber();
	}

	@Override
	public void setStartTime(double timeSecond) {
		// TODO Auto-generated method stub
		timeLine = Math.round(timeSecond*1e6);
	}
	
	@Override
	public String toPrettyString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("upload_packet_size_model:\n");
		sb.append(uploadPacketSize.toPrettyString(true, 0));
		sb.append("\n");
		
		sb.append("download_packet_size_model:\n");
		sb.append(downloadPacketSize.toPrettyString(true, 0));
		sb.append("\n");
		
		sb.append("upload_packet_interval_model:\n");
		sb.append(uploadInterval.toPrettyString(false, 1e-6));
		sb.append("\n");
		
		sb.append("download_packet_interval_model:\n");
		sb.append(downloadInterval.toPrettyString(false, 1e-6));
		sb.append("\n");
		
		return sb.toString();
	}

	@Override
	public XYSeriesCollection createChartDataset() {
		XYSeriesCollection dataset= new XYSeriesCollection();
		
		dataset.addSeries(uploadPacketSize.createChartSeries("upload_packet_size_model"));
		dataset.addSeries(downloadPacketSize.createChartSeries("download_packet_size_model"));
		dataset.addSeries(uploadInterval.createChartSeries("upload_packet_interval_model"));
		dataset.addSeries(downloadInterval.createChartSeries("download_packet_interval_model"));
		
		return dataset;
	}
}
