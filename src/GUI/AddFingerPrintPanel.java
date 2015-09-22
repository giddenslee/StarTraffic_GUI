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
		
		JLabel lbl1 = new JLabel("流量文件/路径");
		lbl1.setBounds(50, 50, 100, 30);
		this.add(lbl1);
		pathpacp = new PathField(true, true, new ExtensionFileFilter("流量文件(pcap)", "pcap"));
		pathpacp.setPath(Setting.getWorkspacePath()+"traffics");
		pathpacp.setBounds(200, 50, 300, 30);
		this.add(pathpacp);
		
		JLabel lbl2 = new JLabel("端口文件/路径");
		lbl2.setBounds(50, 100, 100, 30);
		this.add(lbl2);
		pathport = new PathField(true, true, new ExtensionFileFilter("端口文件(txt)", "txt"));
		pathport.setPath(Setting.getWorkspacePath()+"traffics");
		pathport.setBounds(200,100,300,30);
		this.add(pathport);
		
		JButton jButton = new JButton("开始新增");
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
			    		Object[] optionsObjects = {"是的","不了，那我不更新"};
			    		int result = JOptionPane.showOptionDialog(null,
			                    "检测到本次指纹["+fingerprintString+"]已经存在，是否更新?","指纹更新", JOptionPane.YES_NO_CANCEL_OPTION,
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
					JOptionPane.showMessageDialog(null,  "指纹收录完成!");
			}

			void HttpResolve(String inFile, String outFile) {
				//打开输入文件
				File resFile = new File(inFile);
				if (!resFile.exists()) {
					System.out.println("error");
					return;
				}
				//打开输出文件
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
					//特征字符串
					String resultStr = null;
					String[] strArray = null;
					String httpFlag = null;
					String[] tempArray1 = null;
					String[] tempArray2 = null;
					//服务器ip
					String serverAddr = null;
					//本地端口
					String sourcePort = null;
					String host = null;
					String user_agent = null;
					//当前包
					int line = 0;
					//当前http包
					int httpLine = 0;
					// 一次读入一行，直到读入null为文件结束,将特征字符串放入map中
					while ((tempString = reader.readLine()) != null) {
						line++;
						strArray = tempString.split(" ");
						httpFlag = strArray[5];
						// 如果是http协议
						if (httpFlag.equals("true")) {
							httpLine++;
							tempArray1 = strArray[3].split(":");
							tempArray2 = strArray[4].split(":");
							// 找服务器地址
							// 找非80端口
							if (tempArray1[1].equals("80")) {
								serverAddr = tempArray1[0];
								sourcePort = tempArray2[1];
							} else {
								serverAddr = tempArray2[0];
								sourcePort = tempArray1[1];
							}
							// 找host
							host = StringUtils.substringBetween(tempString, "HOST=[",
									"]");
							// 找user-agent
							user_agent = StringUtils.substringBetween(tempString,
									"USER-AGENT=[", "]");
							// 拼接
							resultStr = "HOST=[" + host + "] USER-AGENT=[" + user_agent
									+ "]";
							Integer id = new Integer(strArray[0]);
							increaseCount(map, resultStr,id);
						}

					}
					double a = ((double) httpLine / line)*100;
					System.out.println("http包占"+a+"%");
					
					fwer = new FileWriter(countFile, true);
					bwer = new BufferedWriter(fwer);
					/*
					 //这里写map排序
					//map排序，按频率由高到低
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
					//遍历map写文件
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
					// http组数组
					HttpGroup[] httpArray = new HttpGroup[8192];
					// 当前组数
					int HttpGroupNum = 0;
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
						// 命令行输出
						/*
						System.out.println(" 当前序号: " + packet.getFrameNumber()
								+ " 到达时间: "
								+ packet.getCaptureHeader().timestampInMillis()
								+ " 包的大小: " + packet.getPacketWirelen() + " 源IP: " + sp
								+ " 目的IP: " + dp + " 是否含HTTP协议头: " + this.isHttp);
					
						
						if (this.isHttp) {
							System.out.printf("http header::%s%n", this.http);
							System.out.println(this.http.contentType());
						}
						*/
						
						// 写入文件
						try {
//							fw = new FileWriter(resFile, true);
//							bw = new BufferedWriter(fw);

							// 写文件start
							// 如果http组不为空
							if (HttpGroupNum > 0) {
								int i = 0;
								// 遍历http组,重最后一个往前遍历
								for (i = HttpGroupNum - 1; i >= 0; i--) {
									// 如果含有http头
									if (this.isHttp) {
										// 如果是request请求
										if (this.isRequest) {
											// 地址端口一致，找到所属httpGroup
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
										} else {// 如果是response请求
											// 地址端口恰好相反，说明找到httpGroup
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
									} else {// 如果不含http头
										if (this.isTcp) {// 传输层为tcp
											// ip端口恰好相同或者相反，说明找到httpGroup
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
								//遍历数组也没找到
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
								// httpGroup为空，说明当前包不可能是http报文
								bw.write(packet.getFrameNumber() + " "
										+ packet.getCaptureHeader().timestampInMillis()
										+ " " + packet.getPacketWirelen() + " " + sp
										+ " " + dp + " " + false);
								bw.newLine();
							}
							// 写文件end

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}, errbuf);
				bw.close();
				// 关闭pcap
				pcap.close();
			}
			
		});
		this.add(jButton);
		
	   this.setTitle("新增指纹");
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
