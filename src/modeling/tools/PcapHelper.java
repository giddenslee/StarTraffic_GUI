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
		// ��pcap�ļ�
		final String FILENAME = pcapFile;
		final StringBuilder errbuf = new StringBuilder();
		final Pcap pcap = Pcap.openOffline(FILENAME, errbuf);
		if (pcap == null) {
			System.err.println(errbuf);
			return;
		}

		// �򿪽���ļ�
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

		// �������а����н���
		pcap.loop(Pcap.LOOP_INFINITE, new JPacketHandler<StringBuilder>() {

			// �Ƿ���httpЭ��ͷ��־
			boolean isHttp = false;
			// �Ƿ�Request����
			boolean isRequest = false;
			// �Ƿ�Response����
			boolean isResponse = false;
			// �Ƿ�tcpЭ��
			boolean isTcp = false;
			// �Ƿ�udpЭ��
			boolean isUdp = false;
			// ipЭ��ͷ
			final Ip4 ip = new Ip4();
			// tcpЭ��ͷ
			final Tcp tcp = new Tcp();
			// httpЭ��ͷ
			final Http http = new Http();
			// udpЭ��ͷ
			final Udp udp = new Udp();

			@Override
			public void nextPacket(JPacket packet, StringBuilder errbuf) {
				// ��IP
				String sipStr = "";
				String dipStr = "";
				if (packet.hasHeader(this.ip)) {
					sipStr = FormatUtils.ip(this.ip.source());
					dipStr = FormatUtils.ip(this.ip.destination());
				}
				// ��port
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
				// ip��ַ+�˿�
				String sp = sipStr + ":" + sPort;
				String dp = dipStr + ":" + dPort;
				// ����httpͷ
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
				 * // ��������� System.out.println(" ��ǰ���: " +
				 * packet.getFrameNumber() + " ����ʱ��: " +
				 * packet.getCaptureHeader().timestampInMillis() + " ���Ĵ�С: " +
				 * packet.getPacketWirelen() + " ԴIP: " + sp + " Ŀ��IP: " + dp +
				 * " �Ƿ�HTTPЭ��ͷ: " + this.isHttp);
				 * 
				 * if (this.isTcp) { System.out.printf("http header::%s%n",
				 * this.tcp); } if (this.isHttp) {
				 * System.out.printf("http header::%s%n", this.http);
				 * System.out.println(this.http.contentType()); }
				 */
				// д���ļ�

				// д�ļ�start
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
				// д�ļ�end
			}

		}, errbuf);

		bw.close();
		// �ر�pcap
		pcap.close();
	}
}
