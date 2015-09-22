package modeling.model.traffic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeriesCollection;

import modeling.model.distribution.Distribution;
import modeling.model.distribution.DistributionExponential;
import modeling.model.distribution.DistributionLinear;
import modeling.packet.PacketInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrafficModelSession extends TrafficModel {
	@Expose
	@SerializedName("upload_session_length_model")
	Distribution sessionLengthUpload;
	@Expose
	@SerializedName("download_session_length_model")
	Distribution sessionLengthDownload;

	@Expose
	@SerializedName("session_interval_model")
	Distribution sessionInterval;

	@Expose
	@SerializedName("upload_packet_interval_model")
	Distribution uploadInterval;
	@Expose
	@SerializedName("download_packet_interval_model")
	Distribution downloadInterval;

	@Expose
	@SerializedName("upload_packet_size_model")
	Distribution uploadPacketSize;
	@Expose
	@SerializedName("download_packet_size_model")
	Distribution downloadPacketSize;

	@Expose
	int MTU = 1460;

	public TrafficModelSession() {
		this(100, 100);
	}

	public TrafficModelSession(double precisionSize, double precisionTime) {
		if (precisionSize < 1)
			precisionSize = 1;
		if (precisionSize > 100)
			precisionSize = 100;
		if (precisionTime < 1)
			precisionTime = 1;
		if (precisionTime > 100)
			precisionTime = 100;

		sessionLengthUpload = new DistributionExponential(precisionSize);
		sessionLengthDownload = new DistributionExponential(precisionSize);
		sessionInterval = new DistributionExponential(precisionTime);
		uploadInterval = new DistributionExponential(precisionTime);
		downloadInterval = new DistributionExponential(precisionTime);
		uploadPacketSize = new DistributionLinear(10);
		downloadPacketSize = new DistributionLinear(10);

		reset();
	}

	int synPacketSize = 74;
	int synAckPacketSize = 66;
	int ackPacketSize = 54;
	int finPacketSize = 54;

	public void setControlPacketSize(int syn, int synack, int ack, int fin) {
		synPacketSize = syn;
		synAckPacketSize = synack;
		ackPacketSize = ack;
		finPacketSize = fin;
	}

	@Override
	public void buildModelFromPackets(List<PacketInfo> packets) {

		HashMap<Integer, Long> lastPortUpload = new HashMap<Integer, Long>();
		HashMap<Integer, Long> lastPortDownload = new HashMap<Integer, Long>();

		HashMap<Integer, Boolean> portOpened = new HashMap<Integer, Boolean>();

		int maxlen = 0, minlen = Integer.MAX_VALUE;

		long lastSYN = 0;
		for (PacketInfo packet : packets) {
			// 收集流间隔信息
			if ((packet.flags & PacketInfo.FLAG_SYN) != 0) {
				if (packet.upload) {
					if (lastSYN != 0)
						sessionInterval.addValue(packet.time - lastSYN);
					lastSYN = packet.time;
				}
			}

			if (packet.upload) {
				uploadPacketSize.addValue(packet.length);
			} else {
				downloadPacketSize.addValue(packet.length);
			}

			// 收集流内部间隔信息
			if ((packet.flags & PacketInfo.FLAG_FIN) == 0) {
				long interval = 0;
				if (packet.upload) {
					if (lastPortUpload.get(packet.localPort) != null)
						interval = packet.time
								- lastPortUpload.get(packet.localPort);
					if (interval > 0)
						uploadInterval.addValue(interval);
				} else {
					if (lastPortDownload.get(packet.localPort) != null)
						interval = packet.time
								- lastPortDownload.get(packet.localPort);
					if (interval > 0)
						downloadInterval.addValue(interval);
				}
			}

			if ((packet.flags & PacketInfo.FLAG_FIN) != 0) {
				lastPortUpload.put(packet.localPort, null);
				lastPortDownload.put(packet.localPort, null);
			} else {
				if (packet.upload)
					lastPortUpload.put(packet.localPort, packet.time);
				else
					lastPortDownload.put(packet.localPort, packet.time);
			}

			// 处理流大小信息
			if ((packet.flags & PacketInfo.FLAG_SYN) != 0)
				portOpened.put(packet.localPort, true);

			if ((packet.flags & PacketInfo.FLAG_FIN) != 0
					&& Boolean.TRUE.equals(portOpened.get(packet.localPort))) {

				long sizeA = packet.seqNum - 1;
				long sizeB = packet.ackNum - 1;

				if (packet.upload) {
					if (sizeA >= 0)
						sessionLengthUpload.addValue(sizeA);
					if (sizeB >= 0)
						sessionLengthDownload.addValue(sizeB);
				} else {
					if (sizeB >= 0)
						sessionLengthUpload.addValue(sizeB);
					if (sizeA >= 0)
						sessionLengthDownload.addValue(sizeA);
				}

				portOpened.put(packet.localPort, false);
			}

			maxlen = Math.max(maxlen, packet.length);
			minlen = Math.min(minlen, packet.length);
		}

		if (maxlen != 0 && maxlen != minlen)
			MTU = maxlen - minlen;

		// System.out.println("MTU:"+MTU);

		sessionLengthUpload.buildModel();
		sessionLengthDownload.buildModel();
		sessionInterval.buildModel();
		uploadInterval.buildModel();
		downloadInterval.buildModel();
		uploadPacketSize.buildModel();
		downloadPacketSize.buildModel();
	}

	double timeLine = 0;
	ArrayList<PacketInfo> packetBuffer;

	@Override
	public List<PacketInfo> generatePackets(double endTimeSecond) {
		double endTime = endTimeSecond * 1000000; // 以微秒表示时间

		ArrayList<PacketInfo> result = packetBuffer;
		packetBuffer = new ArrayList<PacketInfo>();

		while (timeLine <= endTime) {
			generateFlow(timeLine, endTime, result, packetBuffer);
			long session_interval = Math.round(sessionInterval.predictValue());

			// System.out.println(session_interval);

			timeLine += session_interval;
		}

		Collections.sort(result);

		return result;
	}

	private void generateFlow(double startTime, double endTime,
			ArrayList<PacketInfo> resultList, ArrayList<PacketInfo> bufferList) {
		double time = startTime;
		int state = 0;
		int upLeft = 0;
		int downLeft = 0;
		double nextU = time;
		double nextD = time;

		PacketInfo packet;
		while (true) {
			if (state == 0) {
				upLeft = (int) sessionLengthUpload.predictValue();
				downLeft = (int) sessionLengthDownload.predictValue();

				// SYN
				packet = new PacketInfo((long) time, synPacketSize, true);
				addGeneratedPacket(packet, endTime, resultList, bufferList);
				time += Math.round(downloadInterval.predictValue());

				// SYNACK
				packet = new PacketInfo((long) time, synAckPacketSize, false);
				addGeneratedPacket(packet, endTime, resultList, bufferList);
				time += Math.round(uploadInterval.predictValue());

				// ACK
				packet = new PacketInfo((long) time, ackPacketSize, true);
				addGeneratedPacket(packet, endTime, resultList, bufferList);

				nextU = time + Math.round(uploadInterval.predictValue());
				nextD = time
						+ Math.round(uploadInterval.predictValue() / 2
								+ downloadInterval.predictValue());
				state = 1;
			} else if (state == 1) {
				if (upLeft <= 0 && downLeft <= 0) {
					state = 2;
				} else if (nextU < nextD) {
					time = nextU;

					//int size = Math.min( MTU , upLeft);
					int size = (int) uploadPacketSize.predictValue() - ackPacketSize;
					if (size<0) size = 0;
					
					//System.out.println("U"+size + "L"+upLeft);
					upLeft -= size;

					packet = new PacketInfo((long) time, size + ackPacketSize,
							true);
					addGeneratedPacket(packet, endTime, resultList, bufferList);

					nextU += Math.round(uploadInterval.predictValue());

					// System.out.println(nextU - time);
				} else {
					time = nextD;
					
					//int size = Math.min( MTU - ackPacketSize, downLeft);
					int size = (int) downloadPacketSize.predictValue() - ackPacketSize;
					if (size<0) size = 0;
					
					//System.out.println("D"+size + "L"+downLeft);
					downLeft -= size;

					packet = new PacketInfo((long) time, size + ackPacketSize,
							false);
					addGeneratedPacket(packet, endTime, resultList, bufferList);

					nextD += Math.round(downloadInterval.predictValue());
				}

				// System.out.println(upLeft+","+downLeft);
			} else if (state == 2) {
				time += Math.round(uploadInterval.predictValue());
				packet = new PacketInfo((long) time, finPacketSize, true);
				addGeneratedPacket(packet, endTime, resultList, bufferList);

				time += Math.round(downloadInterval.predictValue());
				packet = new PacketInfo((long) time, finPacketSize, false);
				addGeneratedPacket(packet, endTime, resultList, bufferList);
				break;
			}
		}
	}

	private void addGeneratedPacket(PacketInfo packet, double endTime,
			ArrayList<PacketInfo> resultList, ArrayList<PacketInfo> bufferList) {
		if (packet.time <= endTime)
			resultList.add(packet);
		else
			bufferList.add(packet);
	}

	@Override
	public void reset() {
		timeLine = 0;
		packetBuffer = new ArrayList<PacketInfo>();
	}

	@Override
	public int getParameterNumber() {
		return sessionInterval.getParameterNumber()
				+ sessionLengthUpload.getParameterNumber()
				+ sessionLengthDownload.getParameterNumber()
				+ uploadInterval.getParameterNumber()
				+ downloadInterval.getParameterNumber();
	}

	@Override
	public void setStartTime(double timeSecond) {
		timeLine = Math.round(timeSecond * 1e6);
	}

	@Override
	public String toPrettyString() {
		StringBuilder sb = new StringBuilder();
		sb.append("upload_session_length_model:\n");
		sb.append(sessionLengthUpload.toPrettyString(true, 0));
		sb.append("\n");

		sb.append("download_session_length_model:\n");
		sb.append(sessionLengthDownload.toPrettyString(true, 0));
		sb.append("\n");

		sb.append("session_interval_model:\n");
		sb.append(sessionInterval.toPrettyString(false, 1e-6));
		sb.append("\n");

		sb.append("upload_packet_interval_model:\n");
		sb.append(uploadInterval.toPrettyString(false, 1e-6));
		sb.append("\n");

		sb.append("download_packet_interval_model:\n");
		sb.append(downloadInterval.toPrettyString(false, 1e-6));
		sb.append("\n");

		sb.append("MTU: " + MTU);
		sb.append("\n");

		return sb.toString();
	}

	@Override
	public XYSeriesCollection createChartDataset() {
		XYSeriesCollection dataset = new XYSeriesCollection();

		dataset.addSeries(sessionLengthUpload
				.createChartSeries("upload_session_length_model"));
		dataset.addSeries(sessionLengthDownload
				.createChartSeries("download_session_length_model"));
		dataset.addSeries(sessionInterval
				.createChartSeries("session_interval_model"));
		dataset.addSeries(uploadInterval
				.createChartSeries("upload_packet_interval_model"));
		dataset.addSeries(downloadInterval
				.createChartSeries("download_packet_interval_model"));

		return dataset;
	}
}
