package GUI;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;



import org.apache.commons.lang3.StringUtils;
import org.jfree.ui.ExtensionFileFilter;
import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

public class AddFingerPrintPanel extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PathField pathpacp;
	private PathField pathport;
	
	public AddFingerPrintPanel(final Vector<String> fingerprintStrings, final JList wordList){
		this.setLayout(null);
		
		JLabel lbl1 = new JLabel("�����ļ�/·��");
		lbl1.setBounds(50, 50, 100, 30);
		this.add(lbl1);
		pathpacp = new PathField(true, true, new ExtensionFileFilter("�����ļ�(pcap)", "pcap"));
		pathpacp.setPath(Setting.getWorkspacePath()+"traffics");
		pathpacp.setBounds(200, 50, 300, 30);
		this.add(pathpacp);
		
		JLabel lbl2 = new JLabel("�˿��ļ�/·��");
		lbl2.setBounds(50, 100, 100, 30);
		this.add(lbl2);
		pathport = new PathField(true, true, new ExtensionFileFilter("�˿��ļ�(txt)", "txt"));
		pathport.setPath(Setting.getWorkspacePath()+"traffics");
		pathport.setBounds(200,100,300,30);
		this.add(pathport);
		
		JButton jButton = new JButton("��ʼ����");
		jButton.setBounds(200, 200, 150, 50);
		jButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					DeleteDir(Setting.getWorkspacePath()+"fingerprint\\.temp\\pcaptemp");
					DeleteDir(Setting.getWorkspacePath()+"fingerprint\\.temp\\txttemp");

					start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			private void DeleteDir(String string) {
				// TODO Auto-generated method stub
				File file = new File(string);
				File[] files = file.listFiles();
				for(int i=0;i<files.length;i++)
					files[i].delete();
			}

			private void start() throws IOException {
				// TODO Auto-generated method stub
				String txtfilenameString = pathport.fileChooser.getSelectedFile().getAbsolutePath();
				String pcapfilenameString = pathpacp.fileChooser.getSelectedFile().getAbsolutePath();
				String pcapfilterd = Setting.getWorkspacePath()+"fingerprint\\.temp\\pcaptemp";
				
				HashMap<String, String> nameHashMap = new HashMap<>();
				new AddFingerprintTest(txtfilenameString,pcapfilenameString,pcapfilterd,nameHashMap);
				
				File pcapFile = new File(pcapfilterd);
				File[] files = pcapFile.listFiles();
				
				boolean option = true;
				for(int i=0;i<files.length;i++){
			    	GetFingerprint(files[i].getAbsolutePath(), Setting.getWorkspacePath()+"fingerprint\\.temp\\txttemp\\temp.txt");
			    	String fingerprintString = nameHashMap.get(files[i].getName().split("\\.")[0]);
			    	if(fingerprintStrings.contains(fingerprintString)){
			    		Object[] optionsObjects = {"�ǵ�","���ˣ����Ҳ�����"};
			    		int result = JOptionPane.showOptionDialog(null,
			                    "��⵽����ָ��["+fingerprintString+"]�Ѿ����ڣ��Ƿ����?","ָ�Ƹ���", JOptionPane.YES_NO_CANCEL_OPTION,
			                    JOptionPane.WARNING_MESSAGE, null,optionsObjects,optionsObjects[0]);
			            if(result == 0)
					    	HttpResolve(Setting.getWorkspacePath()+"fingerprint\\.temp\\txttemp\\temp.txt",Setting.getWorkspacePath()+"fingerprint\\fingerprints\\"+fingerprintString+".txt");
			            else {
			            	option = false;
							break;
			            }
			    	}
			    	else{
				    	HttpResolve(Setting.getWorkspacePath()+"fingerprint\\.temp\\txttemp\\temp.txt",Setting.getWorkspacePath()+"fingerprint\\fingerprints\\"+fingerprintString+".txt");
						fingerprintStrings.add(fingerprintString);
			    	}
				}
				
//				for(int i=0;i<fingerprintStrings.size();i++)
//					System.out.println(fingerprintStrings.get(i));
				wordList.updateUI();
				if(option)
					JOptionPane.showMessageDialog(null,  "ָ����¼���!");
			}

			void HttpResolve(String inFile, String outFile) {
				//�������ļ�
				File resFile = new File(inFile);
				if (!resFile.exists()) {
					System.out.println("error");
					return;
				}
				//������ļ�
				File countFile = new File(outFile);
				if (!countFile.exists()) {
					try {
						countFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				BufferedReader reader = null;
				FileReader fr = null;
				FileWriter fwer = null;
				BufferedWriter bwer = null;
				//
				HashMap<String, HashSet<Integer>> map = new HashMap<String, HashSet<Integer>>();
				try {
					fr = new FileReader(resFile);
					reader = new BufferedReader(fr);
					String tempString = null;
					//�����ַ���
					String resultStr = null;
					String[] strArray = null;
					String httpFlag = null;
					String[] tempArray1 = null;
					String[] tempArray2 = null;
					//������ip
					String serverAddr = null;
					//���ض˿�
					String sourcePort = null;
					String host = null;
					String user_agent = null;
					//��ǰ��
					int line = 0;
					//��ǰhttp��
					int httpLine = 0;
					// һ�ζ���һ�У�ֱ������nullΪ�ļ�����,�������ַ�������map��
					while ((tempString = reader.readLine()) != null) {
						line++;
						strArray = tempString.split(" ");
						httpFlag = strArray[5];
						// �����httpЭ��
						if (httpFlag.equals("true")) {
							httpLine++;
							tempArray1 = strArray[3].split(":");
							tempArray2 = strArray[4].split(":");
							// �ҷ�������ַ
							// �ҷ�80�˿�
							if (tempArray1[1].equals("80")) {
								serverAddr = tempArray1[0];
								sourcePort = tempArray2[1];
							} else {
								serverAddr = tempArray2[0];
								sourcePort = tempArray1[1];
							}
							// ��host
							host = StringUtils.substringBetween(tempString, "HOST=[",
									"]");
							// ��user-agent
							user_agent = StringUtils.substringBetween(tempString,
									"USER-AGENT=[", "]");
							// ƴ��
							resultStr = "HOST=[" + host + "] USER-AGENT=[" + user_agent
									+ "]";
							Integer id = new Integer(strArray[0]);
							increaseCount(map, resultStr,id);
						}

					}
					double a = ((double) httpLine / line)*100;
					System.out.println("http��ռ"+a+"%");
					
					fwer = new FileWriter(countFile, true);
					bwer = new BufferedWriter(fwer);
					/*
					 //����дmap����
					//map���򣬰�Ƶ���ɸߵ���
					ArrayList<Map.Entry<String, HashSet<Integer>>> sorted = new ArrayList<>(
							map.entrySet());

					sorted.sort(new Comparator<Map.Entry<String, HashSet<Integer>>>() {
						@Override
						public int compare(Entry<String, HashSet<Integer>> o1,
								Entry<String, HashSet<Integer>> o2) {
							// TODO Auto-generated method stub
							return -o1.getValue().size().compareTo(o2.getValue().size());
						}
					});
					//����mapд�ļ�
					for (Map.Entry<String, Integer> entry : sorted) {
						System.out.println(entry.getKey() + ":" + entry.getValue().toString()+" "+a);
						bwer.write(entry.getKey() + " PacketNums:" +  entry.getValue().g);
						bwer.newLine();
					}
					*/
					for (String key : map.keySet()) {
//						   System.out.println("key= "+ key + " and Num = " + map.get(key).size() +" id set ="+map.get(key).toString());
//						   System.out.println(" id set ="+map.get(key).toString());

//						   bwer.write("key= "+ key + " and Num = " + map.get(key).size() +" id set ="+map.get(key).toString());
						   bwer.write(key + " Num=[" + map.get(key).size() +"]");

						   bwer.newLine();
				  }
					bwer.close();
					fwer.close();
					reader.close();
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			private void increaseCount(HashMap<String, HashSet<Integer>> countMap,
					String key, Integer id) {
				// TODO Auto-generated method stub
				if (countMap.containsKey(key)) {
					HashSet<Integer> idSetFal = countMap.get(key);
					idSetFal.add(id);
					countMap.put(key, idSetFal);
				} else {
					HashSet<Integer> idSet = new HashSet<Integer>();
					idSet.add(id);
					countMap.put(key, idSet);
				}
			}

			void GetFingerprint(String pcapFile, String outFile) throws IOException {
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
					// http������
					HttpGroup[] httpArray = new HttpGroup[8192];
					// ��ǰ����
					int HttpGroupNum = 0;
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
						if (packet.hasHeader(this.tcp)) {
							this.isTcp = true;
							sPort = this.tcp.source();
							dPort = this.tcp.destination();
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
								httpArray[HttpGroupNum] = new HttpGroup();
								httpArray[HttpGroupNum].setUseFlag(true);
								httpArray[HttpGroupNum].setsIpPort(sp);
								httpArray[HttpGroupNum].setdIpPort(dp);
								httpArray[HttpGroupNum].setHostStr(StringUtils
										.substringBetween(this.http.toString(),
												"HOST = ", "\n"));
								httpArray[HttpGroupNum].setUserAgentStr(StringUtils
										.substringBetween(this.http.toString(),
												"USER-AGENT = ", "\n"));
								HttpGroupNum++;
								if (HttpGroupNum > 8191) {
									throw new ArrayIndexOutOfBoundsException();
								}
							}
						} else {
							this.isHttp = false;
							this.isRequest = false;
							this.isResponse = false;
						}
						// ���������
						/*
						System.out.println(" ��ǰ���: " + packet.getFrameNumber()
								+ " ����ʱ��: "
								+ packet.getCaptureHeader().timestampInMillis()
								+ " ���Ĵ�С: " + packet.getPacketWirelen() + " ԴIP: " + sp
								+ " Ŀ��IP: " + dp + " �Ƿ�HTTPЭ��ͷ: " + this.isHttp);
					
						
						if (this.isHttp) {
							System.out.printf("http header::%s%n", this.http);
							System.out.println(this.http.contentType());
						}
						*/
						
						// д���ļ�
						try {
//							fw = new FileWriter(resFile, true);
//							bw = new BufferedWriter(fw);

							// д�ļ�start
							// ���http�鲻Ϊ��
							if (HttpGroupNum > 0) {
								int i = 0;
								// ����http��,�����һ����ǰ����
								for (i = HttpGroupNum - 1; i >= 0; i--) {
									// �������httpͷ
									if (this.isHttp) {
										// �����request����
										if (this.isRequest) {
											// ��ַ�˿�һ�£��ҵ�����httpGroup
											if (sp.equals(httpArray[i].getsIpPort())
													&& dp.equals(httpArray[i]
															.getdIpPort())) {
												bw.write(packet.getFrameNumber()
														+ " "
														+ packet.getCaptureHeader()
																.timestampInMillis()
														+ " "
														+ packet.getPacketWirelen()
														+ " "
														+ sp
														+ " "
														+ dp
														+ " "
														+ true
														+ " HOST=["
														+ httpArray[i].getHostStr()
														+ "] USER-AGENT=["
														+ httpArray[i]
																.getUserAgentStr()
														+ "]");
												bw.newLine();
												break;
											}
										} else {// �����response����
											// ��ַ�˿�ǡ���෴��˵���ҵ�httpGroup
											if (sp.equals(httpArray[i].getdIpPort())
													&& dp.equals(httpArray[i]
															.getsIpPort())) {
												bw.write(packet.getFrameNumber()
														+ " "
														+ packet.getCaptureHeader()
																.timestampInMillis()
														+ " "
														+ packet.getPacketWirelen()
														+ " "
														+ sp
														+ " "
														+ dp
														+ " "
														+ true
														+ " HOST=["
														+ httpArray[i].getHostStr()
														+ "] USER-AGENT=["
														+ httpArray[i]
																.getUserAgentStr()
														+ "]");
												bw.newLine();
												break;
											}
										}
									} else {// �������httpͷ
										if (this.isTcp) {// �����Ϊtcp
											// ip�˿�ǡ����ͬ�����෴��˵���ҵ�httpGroup
											if ((dp.equals(httpArray[i].getdIpPort()) && sp
													.equals(httpArray[i].getsIpPort()))
													|| (sp.equals(httpArray[i]
															.getdIpPort()) && dp
															.equals(httpArray[i]
																	.getsIpPort()))) {
												bw.write(packet.getFrameNumber()
														+ " "
														+ packet.getCaptureHeader()
																.timestampInMillis()
														+ " "
														+ packet.getPacketWirelen()
														+ " "
														+ sp
														+ " "
														+ dp
														+ " "
														+ true
														+ " HOST=["
														+ httpArray[i].getHostStr()
														+ "] USER-AGENT=["
														+ httpArray[i]
																.getUserAgentStr()
														+ "]");
												bw.newLine();
												break;
											}
										}
									}
								}
								//��������Ҳû�ҵ�
								if (i == -1) {
									bw.write(packet.getFrameNumber()
											+ " "
											+ packet.getCaptureHeader()
													.timestampInMillis() + " "
											+ packet.getPacketWirelen() + " " + sp
											+ " " + dp + " " + false);
									bw.newLine();
								}
							} else {
								// httpGroupΪ�գ�˵����ǰ����������http����
								bw.write(packet.getFrameNumber() + " "
										+ packet.getCaptureHeader().timestampInMillis()
										+ " " + packet.getPacketWirelen() + " " + sp
										+ " " + dp + " " + false);
								bw.newLine();
							}
							// д�ļ�end

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}, errbuf);
				bw.close();
				// �ر�pcap
				pcap.close();
			}
			
		});
		this.add(jButton);
		
	   this.setTitle("����ָ��");
	   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	   screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	   this.setPreferredSize(new Dimension(600,400));            
	   int frameWidth = this.getPreferredSize().width;
	   int frameHeight = this.getPreferredSize().height;
	   this.setSize(frameWidth, frameHeight);
	   this.setLocation((screenSize.width - frameWidth) / 2,(screenSize.height - frameHeight) / 2);
	   this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	   
	}
}
