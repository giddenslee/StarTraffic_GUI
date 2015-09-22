package modeling.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

public class PcapHelper {
	public static void packetSummarize(String pcapFile, String outFile)
			throws IOException {
		// 打开pcap文件
		final String FILENAME = pcapFile;
		final StringBuilder errbuf = new StringBuilder();
		final Pcap pcap = Pcap.openOffline(FILENAME, errbuf);
		if (pcap == null) {
			System.err.println(errbuf);
			return;
		}

		// 打开结果文件
		final File resFile = new File(outFile);
		if (!resFile.exists()) {
			try {
				resFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			resFile.delete();
			try {
				resFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		final BufferedWriter bw = new BufferedWriter(new FileWriter(resFile));

		// 遍历所有包进行解析
		pcap.loop(Pcap.LOOP_INFINITE, new JPacketHandler<StringBuilder>() {

			// 是否含有http协议头标志
			boolean isHttp = false;
			// 是否Request请求
			boolean isRequest = false;
			// 是否Response请求
			boolean isResponse = false;
			// 是否tcp协议
			boolean isTcp = false;
			// 是否udp协议
			boolean isUdp = false;
			// ip协议头
			final Ip4 ip = new Ip4();
			// tcp协议头
			final Tcp tcp = new Tcp();
			// http协议头
			final Http http = new Http();
			// udp协议头
			final Udp udp = new Udp();

			@Override
			public void nextPacket(JPacket packet, StringBuilder errbuf) {
				// 求IP
				String sipStr = "";
				String dipStr = "";
				if (packet.hasHeader(this.ip)) {
					sipStr = FormatUtils.ip(this.ip.source());
					dipStr = FormatUtils.ip(this.ip.destination());
				}
				// 求port
				int sPort = 0;
				int dPort = 0;
				String tcpStr = "";
				if (packet.hasHeader(this.tcp)) {
					this.isTcp = true;
					sPort = this.tcp.source();
					dPort = this.tcp.destination();
					tcpStr = "\"" + sPort + ">" + dPort + "\"," + "\"ACK_FLAG:"
							+ this.tcp.flags_ACK() + "\",\"FIN_FLAG:"
							+ this.tcp.flags_FIN() + "\",\"SYN_FLAG:"
							+ this.tcp.flags_SYN() + "\",\"" + this.tcp.seq()
							+ "\",\"" + this.tcp.ack() + "\"";
				} else {
					this.isTcp = false;
				}
				if (packet.hasHeader(this.udp)) {
					this.isUdp = true;
					sPort = this.udp.source();
					dPort = this.udp.destination();
				} else {
					this.isUdp = false;
				}
				// ip地址+端口
				String sp = sipStr + ":" + sPort;
				String dp = dipStr + ":" + dPort;
				// 含有http头
				if (packet.hasHeader(this.tcp) && packet.hasHeader(this.http)) {
					this.isHttp = true;
					if (this.http.isResponse()) {
						this.isRequest = false;
						this.isResponse = true;
					} else {
						this.isRequest = true;
						this.isResponse = false;
					}
				} else {
					this.isHttp = false;
					this.isRequest = false;
					this.isResponse = false;
				}
				/*
				 * // 命令行输出 System.out.println(" 当前序号: " +
				 * packet.getFrameNumber() + " 到达时间: " +
				 * packet.getCaptureHeader().timestampInMillis() + " 包的大小: " +
				 * packet.getPacketWirelen() + " 源IP: " + sp + " 目的IP: " + dp +
				 * " 是否含HTTP协议头: " + this.isHttp);
				 * 
				 * if (this.isTcp) { System.out.printf("http header::%s%n",
				 * this.tcp); } if (this.isHttp) {
				 * System.out.printf("http header::%s%n", this.http);
				 * System.out.println(this.http.contentType()); }
				 */
				// 写入文件

				// 写文件start
				try {
					bw.write("\"" + packet.getFrameNumber() + "\"," + "\""
							+ packet.getCaptureHeader().timestampInMicros() + "\","
							+ "\"" + sipStr + "\"," + "\"" + dipStr + "\"," + "\""
							+ this.isTcp + "\"," + "\"" + packet.getPacketWirelen()
							+ "\"," + tcpStr);
					bw.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 写文件end
			}

		}, errbuf);

		bw.close();
		// 关闭pcap
		pcap.close();
	}
}
