package modeling.packet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import org.jfree.data.xy.XYSeries;

import GUI.FingerprintManagement.doubleClick;
import modeling.packet.PacketInfo;
import modeling.tools.TextInOut;

public class PacketCollector {

	HashSet<String> localIpSet = new HashSet<String>();
	ArrayList<PacketInfo> packetList = new ArrayList<PacketInfo>();
	ArrayList<PacketInfo> nonTcpPackets = new ArrayList<PacketInfo>();

	static int SEQ_UNAVLIABLE = 1000000;

	public HashSet<String> getLocalIpSet() {
		return localIpSet;
	}

	public ArrayList<PacketInfo> getPacketList() {
		return packetList;
	}

	public ArrayList<PacketInfo> getNonTcpPackets() {
		return nonTcpPackets;
	}

	public ArrayList<PacketInfo> collect(String filename) throws IOException {
		ArrayList<String> inputs = TextInOut.readFile(filename);
		ArrayList<PacketInfo> tempList = new ArrayList<PacketInfo>();
		for (String str : inputs) {
			PacketInfo info = PacketInfo.praseTCPInfo(str);
			if (info == null)
				continue;

			tempList.add(info);
			if (info.remotePort == 80 || info.remotePort == 8080) {
				// if (!localIpSet.contains(info.sourceIp))
				// System.out.println(info.sourceIp);
				localIpSet.add(info.sourceIp);
			}
		}

		for (PacketInfo info : tempList) {
			if ("TCP".equals(info.protocol) || "true".equals(info.protocol)) {
				PacketInfo tcpinfo = PacketInfo.fillUpTCPInfo(info, localIpSet);
				if (tcpinfo != null) {
					packetList.add(tcpinfo);
				}
			} else
				nonTcpPackets.add(info);
		}

		packetList = addMissingFlag(packetList);
		packetList = fillRelativeSequenceNumber(packetList);

		return packetList;
	}

	public ArrayList<PacketInfo> addMissingFlag(ArrayList<PacketInfo> packetList) {
		HashSet<Integer> appearPorts = new HashSet<Integer>();
		for (int i = packetList.size() - 1; i >= 0; --i) {
			PacketInfo info = packetList.get(i);
			if (!appearPorts.contains(info.localPort)) {
				appearPorts.add(info.localPort);
				info.flags |= PacketInfo.FLAG_FIN;
			}
		}
		return packetList;
	}

	public ArrayList<PacketInfo> fillRelativeSequenceNumber(
			ArrayList<PacketInfo> packetList) {
		HashMap<Integer, Long> uploadSeq = new HashMap<Integer, Long>();
		HashMap<Integer, Long> uploadSeq2 = new HashMap<Integer, Long>();
		HashMap<Integer, Long> downloadSeq = new HashMap<Integer, Long>();
		HashMap<Integer, Long> downloadSeq2 = new HashMap<Integer, Long>();

		for (PacketInfo info : packetList) {
			int port = info.localPort;
			//
			// if (info.id<10)
			// {
			// if ((info.flags&TCPInfo.FLAG_SYN)!=0)
			// System.out.println(info.id+"  SYN");
			// if ((info.flags&TCPInfo.FLAG_FIN)!=0)
			// System.out.println(info.id+"  FIN");
			// if ((info.flags&TCPInfo.FLAG_ACK)!=0)
			// System.out.println(info.id+"  ACK");
			//
			// System.out.println(info.flags);
			// }

			if ((info.flags & PacketInfo.FLAG_SYN) != 0
					|| !uploadSeq.containsKey(port)
					|| !downloadSeq.containsKey(port)) {
				// System.out.println(port);
				if (info.upload) {
					uploadSeq.put(port, info.seqNum);
					uploadSeq2.put(port, info.seqNum);
					info.seqNum = 0;
					if (info.ackNum != 0) {
						Long num = downloadSeq.get(port);
						if (num != null)
							info.ackNum -= num;
						else {
							downloadSeq.put(port, info.ackNum);
							downloadSeq2.put(port, info.ackNum);
						}
					}
				} else {
					downloadSeq.put(port, info.seqNum);
					downloadSeq2.put(port, info.seqNum);
					info.seqNum = 0;
					if (info.ackNum != 0) {
						Long num = uploadSeq.get(port);
						if (num != null)
							info.ackNum -= num;
						else {
							uploadSeq.put(port, info.ackNum);
							uploadSeq2.put(port, info.ackNum);
						}
					}
				}
			} else {
				if (info.upload) {
					long seqNum = minusSeqNum(info.seqNum, uploadSeq.get(port));
					long ackNum = minusSeqNum(info.ackNum,
							downloadSeq.get(port));

					if (minusSeqNum(info.seqNum, uploadSeq2.get(port)) > SEQ_UNAVLIABLE
							&& Math.abs(info.seqNum - uploadSeq2.get(port)) > SEQ_UNAVLIABLE)
						seqNum = 0;
					else
						uploadSeq2.put(port, info.seqNum);

					if (minusSeqNum(info.ackNum, downloadSeq2.get(port)) > SEQ_UNAVLIABLE
							&& Math.abs(info.ackNum - downloadSeq2.get(port)) > SEQ_UNAVLIABLE)
						ackNum = 0;
					else
						downloadSeq2.put(port, info.ackNum);
					
					info.seqNum = seqNum;
					info.ackNum = ackNum;

				} else {
					long seqNum = minusSeqNum(info.seqNum,
							downloadSeq.get(port));
					long ackNum = minusSeqNum(info.ackNum, uploadSeq.get(port));

					if (minusSeqNum(info.seqNum, downloadSeq2.get(port)) > SEQ_UNAVLIABLE
							&& Math.abs(info.seqNum - downloadSeq2.get(port)) > SEQ_UNAVLIABLE)
						seqNum = 0;
					else
						downloadSeq2.put(port, info.seqNum);

					if (minusSeqNum(info.ackNum, uploadSeq2.get(port)) > SEQ_UNAVLIABLE
							&& Math.abs(info.ackNum - uploadSeq2.get(port)) > SEQ_UNAVLIABLE)
						ackNum = 0;
					else
						uploadSeq2.put(port, info.ackNum);

					info.seqNum = seqNum;
					info.ackNum = ackNum;
				}
			}
		}

		return packetList;
	}

	private long minusSeqNum(long seqNum, long startNum) {
		long result = 0;

		if (seqNum == 0)
			result = 0;
		else if (seqNum >= startNum)
			result = seqNum - startNum;
		else if (seqNum < startNum)
			result = seqNum + (1L << 32) - startNum;

		// if (seqNum < (1L << 31) && startNum >= (1L >> 31))
		// return seqNum + (1L << 32) - startNum;

		// if (result > 100000000)
		// result = 0;

		return result;
	}

	public ArrayList<PacketInfo> fliterRetransmission(
			List<PacketInfo> packetList) {
		ArrayList<PacketInfo> result = new ArrayList<PacketInfo>();

		for (PacketInfo packet : packetList) {
			if (packet.info != null
					&& packet.info.contains("[TCP Retransmission]")) {

			} else {
				result.add(packet);
			}
		}
		return result;
	}

	public static void split(List<PacketInfo> packetList,
			List<PacketInfo> uploadList, List<PacketInfo> downloadList) {
		for (PacketInfo packet : packetList) {
			if (packet.upload) {
				if (uploadList != null)
					uploadList.add(packet);
			} else {
				if (downloadList != null)
					downloadList.add(packet);
			}
		}
	}

	public static void increaseCount(TreeMap<String, Integer> countTree,
			String key) {
		if (!countTree.containsKey(key))
			countTree.put(key, 1);
		else {
			Integer v = countTree.get(key) + 1;
			countTree.put(key, v);
		}
	}

	public static XYSeries createChartSeries(List<PacketInfo> packets,
			String name, double startTimeSecond, double endTimeSecond) {
		XYSeries series = new XYSeries(name, false);
		double endTime = endTimeSecond * 1e6;
		double lastTime = -1;
		double timeZero = Math.floor(startTimeSecond * 1e6);

		for (PacketInfo packet : packets)
			if (packet.time <= endTime) {
				double diff = packet.time - lastTime;

				if (diff > 1.0 && diff <= 2.0) {
					series.add((lastTime + 1 - timeZero) * 1e-6, 0);
				}
				if (diff > 2.0) {
					if (lastTime > 0)
						series.add((lastTime + 1 - timeZero) * 1e-6, 0);
					if (packet.time - 1 - timeZero > 0)
						series.add((packet.time - 1 - timeZero) * 1e-6, 0);
				}
				series.add((packet.time - timeZero) * 1e-6, packet.length);
				lastTime = packet.time;
			}
		series.add((lastTime + 1 - timeZero) * 1e-6, 0);
		series.add((endTime - timeZero) * 1e-6, 0);
		return series;
	}

	public static void WriteToFile(List<PacketInfo> generated, String filename)
			throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter(filename));

		for (PacketInfo info : generated) {
			String line = String.format("%.9f", info.getTimeSecond()) + ","
					+ info.length + "," + info.upload + "\n";
			output.write(line);
		}
		output.close();
	}

	public static void WriteToFiles(List<PacketInfo> generated, String filename)
			throws IOException {

		ArrayList<String> alldata = new ArrayList<String>();
		ArrayList<String> updata = new ArrayList<String>();
		ArrayList<String> dldata = new ArrayList<String>();
		for (int i = 0; i < generated.size(); ++i) {
			PacketInfo info = generated.get(i);
			String line = info.time + "," + info.length + "," + info.upload;

			alldata.add(line);
			if (info.upload)
				updata.add(line);
			else
				dldata.add(line);
		}

		TextInOut.writeFile(alldata, filename + ".gen");
		TextInOut.writeFile(updata, filename + ".up.gen");
		TextInOut.writeFile(dldata, filename + ".dl.gen");
	}

	public static void main(String[] args) throws IOException {
		PacketCollector collector = new PacketCollector();
		ArrayList<PacketInfo> packetList = collector
				.collect("WSexport/youkuTcp.txt");

		ArrayList<String> output = new ArrayList<String>();
		for (PacketInfo info : packetList) {
			output.add(info.id + "," + info.sourceIp + "," + info.destIp + ","
					+ info.seqNum + "," + info.ackNum);
		}
		TextInOut.writeFile(output, "youku.collect.txt");
	}
}
