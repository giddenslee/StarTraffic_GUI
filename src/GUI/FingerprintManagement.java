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
			wordList.setFont(new Font("΢���ź�",Font.PLAIN,20));
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
			jButton_ExportButton = new JButton("����");
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

//			Todo ����������Ϊ�����ļ��������ļ������Ը�
			
//			exportfile.setSelectedFile(testFile);
			
			if(JFileChooser.APPROVE_OPTION == exportfile.showDialog(new JLabel(), "ָ�Ƶ���")){
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
				   // ����д��������
				   OutputStream os = new FileOutputStream(zipFile);
				   BufferedOutputStream bos = new BufferedOutputStream(os);
				   zos = new ZipOutputStream(bos);
				   
				   String basePath = null;
				   
				   // ��ȡĿ¼
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
				  JOptionPane.showMessageDialog(null,  " ָ�Ƶ������!");
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
			jButton_ImportButton = new JButton("����");
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

			if(JFileChooser.APPROVE_OPTION == importfile.showDialog(new JLabel(), "����ָ��")){
				File file = importfile.getSelectedFile();
				try {
					ZipInputStream Zin=new ZipInputStream(new FileInputStream(
							file.getAbsolutePath()));//����Դzip·��
					BufferedInputStream Bin=new BufferedInputStream(Zin);
					String Parent=Setting.getWorkspacePath()+"fingerprint\\fingerprints"; //���·�����ļ���Ŀ¼��
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
				
			  JOptionPane.showMessageDialog(null,  " ָ�Ƶ������!");
			}
		}
	}

	private JButton getjButton_DeleteButton() {
		// TODO Auto-generated method stub
		if(jButton_DeleteButton == null){
			jButton_DeleteButton = new JButton("ɾ��");
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
				JOptionPane.showMessageDialog(null,  " ����ѡ��ָ��!");
			else if(JOptionPane.showConfirmDialog(null, "ȷ��ɾ��ָ��?","ָ��ɾ��",JOptionPane.YES_NO_OPTION) == 0){
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
		    	JOptionPane.showMessageDialog(null,  " ָ��ɾ�����!");
				wordList.updateUI();
				jTextArea.setText("");
			}
		}
	}

	private JButton getjButton_AddButton() {
		// TODO Auto-generated method stub
		if(jButton_AddButton == null){
			jButton_AddButton = new JButton("����");
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
//					fw = new FileWriter(resFile, true);
//					bw = new BufferedWriter(fw);

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

	}
	
}


