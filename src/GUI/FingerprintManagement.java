package GUI;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SelectableChannel;
import java.security.cert.PKIXRevocationChecker.Option;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.naming.spi.DirStateFactory.Result;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;
import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;



public class FingerprintManagement extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFileChooser jFileChooser;
	
	private JButton jButton_AddButton;
	private JButton jButton_DeleteButton;
	private JButton jButton_ImportButton;
	private JButton jButton_ExportButton;
	
	private JList wordList;
    private JScrollPane jScrollPaneFingerprint;
    private JScrollPane jScrollPaneDisplay;
    private JTextArea jTextArea;
    
    Vector<String> fingerprintStrings;
    
	public FingerprintManagement(){
		this.setLayout(null);
		
		Init_fingerprints();
		this.add(getjTextArea(),null);

		this.add(getjscrollPaneFingerprint(),null);
		this.add(getjScrollPaneDisplay(),null);
		this.add(getjButton_AddButton(),null);
		this.add(getjButton_DeleteButton(),null);
		this.add(getjButton_ImportButton(),null);
		this.add(getjButton_ExportButton(),null);
	}
	

	private JScrollPane getjScrollPaneDisplay() {
		// TODO Auto-generated method stub
		if(jScrollPaneDisplay == null){
			jScrollPaneDisplay = new JScrollPane(jTextArea);
			jScrollPaneDisplay.setBounds(200, 30, 980, 670);
		}
		return jScrollPaneDisplay;
	}


	private JTextArea getjTextArea() {
		// TODO Auto-generated method stub
		if(jTextArea == null){
			jTextArea = new JTextArea();
			jTextArea.setBounds(200, 30, 980, 670);
		}
		return jTextArea;
	}


	private void Init_fingerprints() {
		// TODO Auto-generated method stub
		if(fingerprintStrings == null){
			fingerprintStrings = new Vector<>();
		}
	}


	private JScrollPane getjscrollPaneFingerprint() {
		// TODO Auto-generated method stub
    	String filenameString = Setting.getWorkspacePath()+"fingerprint\\fingerprints";
        try {
            File file = new File(filenameString);
            if(file.isDirectory()){
            	File[] files = file.listFiles();
            	for(int i=0;i<files.length;i++)
            		fingerprintStrings.add(files[i].getName().split("\\.")[0]);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        
    	if(wordList == null){
    		wordList = new JList(fingerprintStrings);
			wordList.setFont(new Font("微软雅黑",Font.PLAIN,20));
			wordList.setAlignmentY(CENTER_ALIGNMENT);
			wordList.addMouseListener(new doubleClick());
    	}
		
		if(jScrollPaneFingerprint == null){
			jScrollPaneFingerprint = new JScrollPane(wordList);
			jScrollPaneFingerprint.setBounds(0, 0, 200, 700);
		}
		return jScrollPaneFingerprint;
	}

	public class doubleClick extends MouseAdapter {
	    public void mouseClicked(MouseEvent e) {
	        if(e.getClickCount() == 1) {
//	        	e.getSource().toString()
//	        	jTextArea.append("\n"+wordList[((JList)e.getSource()).getSelectedIndex()]);
	        	jTextArea.setText(null);
	        	String filenameString = Setting.getWorkspacePath()+"fingerprint\\fingerprints\\" + fingerprintStrings.get(((JList)e.getSource()).getSelectedIndex()) +".txt";
		        try {
		            File f1 = new File(filenameString);
		            BufferedReader in = new BufferedReader(new FileReader(f1));
		            String str1 = in.readLine();
		            while (str1 != null) {
		                jTextArea.append("\n"+str1);
		                str1 = in.readLine(); 
		            }
		            in.close();
		        } catch (Exception exception) {
		            exception.printStackTrace();
		        }
//	            text.append("\n"+times[((JList)e.getSource()).getSelectedIndex()]);
	        }
	    }
	}

	private JButton getjButton_ExportButton() {
		// TODO Auto-generated method stub
		if(jButton_ExportButton == null){
			jButton_ExportButton = new JButton("导出");
			jButton_ExportButton.setBounds(950, 0, 250, 30);
			jButton_ExportButton.addActionListener(new HellojButton_ExportButton());
		}
		return jButton_ExportButton;
	}

	public class HellojButton_ExportButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
			JFileChooser exportfile  = new JFileChooser();
			exportfile.setFileSelectionMode(JFileChooser.SAVE_DIALOG | JFileChooser.FILES_ONLY);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("zip", "zip");
			exportfile.setFileFilter(filter);
			exportfile.setCurrentDirectory(new File(Setting.getWorkspacePath()));

//			Todo 将导出设置为保存文件，并且文件名可以改
			
//			exportfile.setSelectedFile(testFile);
			
			if(JFileChooser.APPROVE_OPTION == exportfile.showDialog(new JLabel(), "指纹导出")){
				String filepath = exportfile.getSelectedFile().getAbsolutePath();
				String filename = exportfile.getSelectedFile().getName();
				
				if(filename.split("\\.").length == 1)
					filepath += ".zip";
				
//				SimpleDateFormat formatter = new SimpleDateFormat("yyyy MM dd-HH mm ss");
				File zipFile = new File(filepath);

//				File zipFile = new File(filename);

//				File file = new File("D:\\fingerprint");
				File file = new File(Setting.getWorkspacePath()+"fingerprint\\fingerprints");

				  ZipOutputStream zos = null;
				  try {
				   // 创建写出流操作
				   OutputStream os = new FileOutputStream(zipFile);
				   BufferedOutputStream bos = new BufferedOutputStream(os);
				   zos = new ZipOutputStream(bos);
				   
				   String basePath = null;
				   
				   // 获取目录
				   if(file.isDirectory()) {
				    basePath = file.getPath();
				   }else {
				    basePath = file.getParent();
				   }
				   
				   zipFile(file, basePath, zos);
				  } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
				   if(zos != null) {
				    try {
						zos.closeEntry();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    try {
						zos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				   }
				  }
				  JOptionPane.showMessageDialog(null,  " 指纹导出完成!");
			 }
		}

		private void zipFile(File source, String basePath, ZipOutputStream zos) 
				 throws IOException {
				  File[] files = null;
				  if (source.isDirectory()) {
				   files = source.listFiles();
				  } else {
				   files = new File[1];
				   files[0] = source;
				  }
				  
				  InputStream is = null;
				  String pathName;
				  byte[] buf = new byte[1024];
				  int length = 0;
				  try{
				   for(File file : files) {
				    if(file.isDirectory()) {
				     pathName = file.getPath().substring(basePath.length() + 1) + "/";
				     zos.putNextEntry(new ZipEntry(pathName));
				     zipFile(file, basePath, zos);
				    }else {
				     pathName = file.getPath().substring(basePath.length() + 1);
				     is = new FileInputStream(file);
				     BufferedInputStream bis = new BufferedInputStream(is);
				     zos.putNextEntry(new ZipEntry(pathName));
				     while ((length = bis.read(buf)) > 0) {
				      zos.write(buf, 0, length);
				     }
				    }
				   }
				  }finally {
				   if(is != null) {
				    is.close();
				   }
				  }
				 }
	}
	
	private JButton getjButton_ImportButton() {
		// TODO Auto-generated method stub
		if(jButton_ImportButton == null){
			jButton_ImportButton = new JButton("导入");
			jButton_ImportButton.setBounds(700, 0, 250, 30);
			jButton_ImportButton.addActionListener(new HellojButton_ImportButton());
		}
		return jButton_ImportButton;
	}

	public class HellojButton_ImportButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			JFileChooser importfile  = new JFileChooser();
			importfile.setFileSelectionMode(JFileChooser.FILES_ONLY);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("zip", "zip");
			importfile.setFileFilter(filter);
			importfile.setCurrentDirectory(new File(Setting.getWorkspacePath()));

			if(JFileChooser.APPROVE_OPTION == importfile.showDialog(new JLabel(), "导入指纹")){
				File file = importfile.getSelectedFile();
				try {
					ZipInputStream Zin=new ZipInputStream(new FileInputStream(
							file.getAbsolutePath()));//输入源zip路径
					BufferedInputStream Bin=new BufferedInputStream(Zin);
					String Parent=Setting.getWorkspacePath()+"fingerprint\\fingerprints"; //输出路径（文件夹目录）
					File Fout=null;
					ZipEntry entry;
					try {
						while((entry = Zin.getNextEntry())!=null && !entry.isDirectory()){
							Fout=new File(Parent,entry.getName());
							if(!Fout.exists()){
								(new File(Fout.getParent())).mkdirs();
							}
							FileOutputStream out=new FileOutputStream(Fout);
							BufferedOutputStream Bout=new BufferedOutputStream(out);
							int b;
							while((b=Bin.read())!=-1){
								Bout.write(b);
							}
							Bout.close();
							out.close();
						}
						Bin.close();
						Zin.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		    	String filenameString = Setting.getWorkspacePath()+"fingerprint\\fingerprints";
		        try {
		            File fingerprintfile = new File(filenameString);
		            if(fingerprintfile.isDirectory()){
		            	File[] files = fingerprintfile.listFiles();
		            	for(int i=0;i<files.length;i++)
		            		fingerprintStrings.add(files[i].getName().split("\\.")[0]);
		            }
		        } catch (Exception exception) {
		            exception.printStackTrace();
		        }
		        wordList.updateUI();
				
			  JOptionPane.showMessageDialog(null,  " 指纹导入完成!");
			}
		}
	}

	private JButton getjButton_DeleteButton() {
		// TODO Auto-generated method stub
		if(jButton_DeleteButton == null){
			jButton_DeleteButton = new JButton("删除");
			jButton_DeleteButton.setBounds(450, 0, 250, 30);
			jButton_DeleteButton.addActionListener(new HellojButton_DeleteButton());
			
		}
		return jButton_DeleteButton;
	}

	public class HellojButton_DeleteButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if(wordList.getSelectedIndices().length == 0)
				JOptionPane.showMessageDialog(null,  " 请先选中指纹!");
			else if(JOptionPane.showConfirmDialog(null, "确定删除指纹?","指纹删除",JOptionPane.YES_NO_OPTION) == 0){
				int[] removeindexs = wordList.getSelectedIndices();
				for(int i=0;i<removeindexs.length;i++){
	//				Delete Files .
					String filename = fingerprintStrings.get(removeindexs[i]-i);
//					File file = new File("D:\\fingerprint\\"+filename+".txt");
					File file = new File(Setting.getWorkspacePath()+"fingerprint\\fingerprints\\"+filename+".txt");

					if(file.exists())
						file.delete();
					fingerprintStrings.remove(removeindexs[i]-i);
				}
		    	JOptionPane.showMessageDialog(null,  " 指纹删除完成!");
				wordList.updateUI();
				jTextArea.setText("");
			}
		}
	}

	private JButton getjButton_AddButton() {
		// TODO Auto-generated method stub
		if(jButton_AddButton == null){
			jButton_AddButton = new JButton("新增");
			jButton_AddButton.setBounds(200, 0, 250, 30);
			jButton_AddButton.addActionListener(new HellojButton_AddButton());
		}
		return jButton_AddButton;
	}

	public class HellojButton_AddButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
			AddFingerPrintPanel jPanel = new AddFingerPrintPanel(fingerprintStrings,wordList);
			jPanel.setVisible(true);
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
			} else {
				countFile.delete();
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
//					   System.out.println("key= "+ key + " and Num = " + map.get(key).size() +" id set ="+map.get(key).toString());
//					   System.out.println(" id set ="+map.get(key).toString());

//					   bwer.write("key= "+ key + " and Num = " + map.get(key).size() +" id set ="+map.get(key).toString());
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
//					fw = new FileWriter(resFile, true);
//					bw = new BufferedWriter(fw);

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

	}
	
}


