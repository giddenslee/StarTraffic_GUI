package GUI;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.commons.lang3.StringUtils;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.ExtensionFileFilter;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapDumper;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import GUI.FingerprintManagement.doubleClick;


class TrafficClassification extends JPanel{
	private JLabel jLabel;
	private JFileChooser jFile_AddTestfile;

	private PathField filePathField;
	private JButton jButton_SubmitSplit;

	//	�ָ���
	private JLabel jLabel_split_0;
	
	static int number;
	
	private DefaultPieDataset dataset;
    private JScrollPane jScrollPaneTable;
    private DefaultTableModel tableModel;
    private JTable jTable;

	public TrafficClassification(){
		
		   this.setLayout(null);
		   
		   this.add(getfilePathField(),null);
		   this.add(getjLabel(),null);
		   this.add(getjButton_SubmitSplit(),null);
		   
	       dataset = new DefaultPieDataset();
		   this.add(new PieChart(dataset).getChartPanel(),null);
		   this.add(getjScrollPaneTable(),null);
		   
//		   �ָ��
		   this.add(getjLabel_split_0(),null);
		   
}
	
private PathField getfilePathField() {
		// TODO Auto-generated method stub
		if(filePathField == null){
			filePathField = new PathField(true, false, new ExtensionFileFilter("pcap�����ļ�", "pcap"));
			filePathField.setPath(Setting.getWorkspacePath()+"traffics");
			filePathField.setBounds(250, 50, 300, 30);
			this.add(filePathField);
		}
		return filePathField;
	}

@SuppressWarnings("serial")
private JScrollPane getjScrollPaneTable() {
		// TODO Auto-generated method stub
		if(jTable == null){
			Object[][] data = {};
			String[] columnNames = { "App", "����" ,"�ٷֱ�"};
			tableModel = new DefaultTableModel(data,columnNames){
				public boolean isCellEditable(int row,int column){
					return false;
				}
			};
			jTable = new JTable(tableModel);
		}
		if(jScrollPaneTable == null){
			jScrollPaneTable = new JScrollPane();
			jScrollPaneTable.setViewportView(jTable);
			jScrollPaneTable.setBounds(650, 240, 300, 400);
		}
		return jScrollPaneTable;
	}

private javax.swing.JLabel getjLabel_split_0() {
		// TODO Auto-generated method stub
	   if(jLabel_split_0 == null) {
		   jLabel_split_0 = new javax.swing.JLabel();
		   jLabel_split_0.setBounds(50, 200, 1400, 30);
		   jLabel_split_0.setText("****************************************************************************************************************************************************************************************************************");
	   }
	   return jLabel_split_0;
	}

private javax.swing.JButton getjButton_SubmitSplit() {
		// TODO Auto-generated method stub
	   if(jButton_SubmitSplit == null) {
		   jButton_SubmitSplit = new javax.swing.JButton();
		   jButton_SubmitSplit.setBounds(650, 50, 100,30);
		   jButton_SubmitSplit.setText("��ʼ����");
		   jButton_SubmitSplit.addActionListener(new HellojButton_SubmitSplit());
	   }
	   return jButton_SubmitSplit;
	}

private javax.swing.JLabel getjLabel() {
		// TODO Auto-generated method stub
	   if(jLabel == null) {
		   jLabel = new javax.swing.JLabel();
		   jLabel.setBounds(120, 50, 150, 30);
		   jLabel.setText("�������ļ�");
	   }
	   return jLabel;
	}


private class HellojButton_SubmitSplit implements ActionListener{
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		jFile_AddTestfile = filePathField.fileChooser;
		if(jFile_AddTestfile == null)
			JOptionPane.showMessageDialog(null, "��ָ�������ļ�!", "��������", JOptionPane.INFORMATION_MESSAGE);
		else{
			
			System.out.println(tableModel.getRowCount());
			
			if(tableModel.getRowCount() > 0 ){
				String[] rowvalues = {"","",""};
				tableModel.addRow(rowvalues);

//				for(int i=0;i<tableModel.getRowCount();++i)
//					tableModel.removeRow(i);
			}
			
			File file = jFile_AddTestfile.getSelectedFile();
			
//			String submitnameString = "D:\\Classification";
			String submitnameString = Setting.getWorkspacePath()+"classification";

			String tempfile = submitnameString+"\\.temp\\_outfilename.txt";
			String resolvefile = submitnameString+"\\.temp\\"+file.getName().split("\\.")[0]+".txt";
			try {
				GetFingerprint(file.getAbsolutePath(),tempfile);
				HttpResolve(tempfile,resolvefile);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			File deletetempFile = new File(tempfile);
			deletetempFile.delete();
			
			SimpleDateFormat currentTime = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
//			System.out.println(currentTime.format(new Date()));
			File classificationFile = new File(Setting.getWorkspacePath()+"classification\\"+currentTime.format(new Date()));
			if(!classificationFile.exists()){
				classificationFile.mkdirs();
			}
			
			BayesClassification(resolvefile,Setting.getWorkspacePath()+"fingerprint\\fingerprints",classificationFile.getAbsolutePath(),dataset,tableModel);
			
			File deleteresolveFile = new File(resolvefile);
			deleteresolveFile.delete();
			
//			GetHttpNum(outfilename,submitnameString1);
		}
	}

	private void BayesClassification(String testfilepath,String trainfilepath, String outfilename, DefaultPieDataset dataset, DefaultTableModel tableModel) {
		try {
			File testfile = new File(testfilepath);
			File trainfile = new File(trainfilepath);

            HashMap<String,Double> useragentHashMap = new HashMap<String,Double>();
            HashMap<String,Double> hostHashMap = new HashMap<String,Double>();
            HashMap<String,String> bothHashMap = new HashMap<String,String>();
            HashMap<String,Integer> Target = new HashMap<String,Integer>();
            
            HashMap<String,Double> ipHashMap = new HashMap<String,Double>();
            HashMap<String,Integer> ipTotalCount = new HashMap<String,Integer>();

			File[] files = trainfile.listFiles();
			for(int i=0;i<files.length;i++){
				
				System.out.println(files[i].getAbsolutePath());
	
	            InputStreamReader trainread = new InputStreamReader(new FileInputStream(files[i]),"GBK");
	            BufferedReader trainbufferedReader = new BufferedReader(trainread);
	            
	            String trainlineTxt = null;
	            while((trainlineTxt = trainbufferedReader.readLine()) != null){
	            	
	            	String target = files[i].getName().split("\\.")[0];
	            	String host_ori = StringUtils.substringBetween(trainlineTxt,"HOST=[","]");
	            	String useragent_ori = StringUtils.substringBetween(trainlineTxt,"USER-AGENT=[","]");
	            	int fingerprintcount = Integer.valueOf(StringUtils.substringBetween(trainlineTxt,"Num=[","]"));

//	            	String target = trainlineTxt.split(",")[0];
//	            	String host_ori = trainlineTxt.split(",")[1];
//	            	String useragent_ori = trainlineTxt.split(",")[2];

	            	String host = target + "," + host_ori;
	            	String useragent = target + "," + useragent_ori;
	            	
	            	if(Target.containsKey(target)){
	            		int TargetValue = Target.get(target);
	            		TargetValue += fingerprintcount;
	            		Target.put(target, TargetValue);
	            	}
	            	else
	            		Target.put(target, 1);
	            	
	            	if(useragentHashMap.containsKey(useragent)){
	            		double useragentValue = useragentHashMap.get(useragent);
	            		useragentValue += fingerprintcount;
	            		useragentHashMap.put(useragent, useragentValue);
	            	}
	            	else
	            		useragentHashMap.put(useragent, 1.0);
	            	
	            	if(hostHashMap.containsKey(host)){
	            		double hostValue = hostHashMap.get(host);
	            		hostValue += fingerprintcount;
	            		hostHashMap.put(host, hostValue);
	            	}
	            	else
	            		hostHashMap.put(host, 1.0);
	            	
	            	if(!bothHashMap.containsKey(host_ori+","+useragent_ori)){
	            		bothHashMap.put(host_ori+","+useragent_ori, target);
	            	}
	            }
	            trainread.close();
			}
	            
	            InputStreamReader testread = new InputStreamReader(new FileInputStream(testfile),"GBK");
	            BufferedReader testbufferedReader = new BufferedReader(testread);
	            String testlineTxt = null;
	            while((testlineTxt = testbufferedReader.readLine()) != null){

	            	String host = StringUtils.substringBetween(testlineTxt,"HOST=[","]");
	            	String useragent = StringUtils.substringBetween(testlineTxt,"USER-AGENT=[","]");
	            	String ip = StringUtils.substringBetween(testlineTxt,"SERVER-IP=[","]");
	            	
	            	int fingerprintcount = Integer.valueOf(StringUtils.substringBetween(testlineTxt, "Num=[", "]"));
	            	
//	            	String host = testlineTxt.split(",")[1];
//	            	String useragent = testlineTxt.split(",")[2];
//	            	String ip = testlineTxt.split(",")[3];

	            	String target = check_Inside(bothHashMap,host+","+useragent);
	            	if(!target.equals("empty")){
	            		if(ipTotalCount.containsKey(target)){
	            			int count = ipTotalCount.get(target);
	            			count += fingerprintcount;
	            			ipTotalCount.put(target, count);
	            		}
	            		else{
	            			ipTotalCount.put(target,1);
	            		}
	            			
	            		if(ipHashMap.containsKey(target+","+ip)){
	            			double count = ipHashMap.get(target+","+ip);
	            			count += fingerprintcount;
	            			ipHashMap.put(target+","+ip,count);
	            		}
	            		else{
	            			ipHashMap.put(target+","+ip, 1.0);
	            		}
	            	}
	            }
	            testread.close();
            
            update(useragentHashMap,Target);
            update(hostHashMap,Target);
            update(ipHashMap,ipTotalCount);
            
//	            print_HashMap(useragentHashMap);
//	            print_HashMap(hostHashMap);
//	            print_HashMap(ipHashMap);
//	            print_Target(Target);
            
            HashMap<String, HashSet<Integer>> resultHashMap = new HashMap<>();
            double prob = predict(testfile,useragentHashMap,hostHashMap,ipHashMap,Target,resultHashMap,outfilename);
            System.out.println(prob);
            
            UpdateGraphics(resultHashMap,dataset,tableModel);
            
		} catch (Exception e) {
		    System.out.println("��ȡ�ļ����ݳ���");
		    e.printStackTrace();
		}
	}

	private void UpdateGraphics(
			HashMap<String, HashSet<Integer>> resultHashMap,
			DefaultPieDataset dataset, DefaultTableModel tableModel) {
		// TODO Auto-generated method stub
		double count = 0;
		Iterator<String> iterator = resultHashMap.keySet().iterator();
		while(iterator.hasNext()){
			String keyString = iterator.next();
			HashSet<Integer> valueset = resultHashMap.get(keyString);
			dataset.setValue(keyString, valueset.size());
			count += valueset.size();
		}
		
		Iterator<String> iter = resultHashMap.keySet().iterator();
		while(iter.hasNext()){
			String keyString = iter.next();
			int value = resultHashMap.get(keyString).size();
			
			double rate = value/count;
			NumberFormat nf = NumberFormat.getPercentInstance();
			nf.setMinimumFractionDigits(2);//���ñ���С��λ
			nf.setRoundingMode(RoundingMode.HALF_UP); //��������ģʽ
			String percent = nf.format(rate);
			
			String[] rowvalues = {keyString,String.valueOf(value),percent};
			tableModel.addRow(rowvalues);
		}
	}

	private String check_Inside(HashMap<String, String> bothHashMap,
			String string) {
		// TODO Auto-generated method stub
		Iterator<String> iter = bothHashMap.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			String value = bothHashMap.get(key);
			if(key.contains(string))
				return value;
		}
		return "empty";
	}

	private void print_Target(HashMap<String, Integer> target) {
		// TODO Auto-generated method stub
		System.out.println("----Target-----");
		Iterator it_d = target.entrySet().iterator();    
	    while (it_d.hasNext()) {
	    	HashMap.Entry entry_d = (HashMap.Entry) it_d.next();    
	        Object key = entry_d.getKey();    
	        Object value = entry_d.getValue();
		    System.out.println("key:"+key+" value:"+value);
	    }
		System.out.println();
	}

	private void print_HashMap(HashMap<String, Double> hashmap) {
		// TODO Auto-generated method stub
		System.out.println("------");
		Iterator it_d = hashmap.entrySet().iterator();    
	    while (it_d.hasNext()) {
	    	HashMap.Entry entry_d = (HashMap.Entry) it_d.next();    
	        Object key = entry_d.getKey();    
	        Object value = entry_d.getValue();
		    System.out.println("key:"+key+" value:"+value);
	    }
	}

	private double predict(File testfile,
			HashMap<String, Double> useragentHashMap,
			HashMap<String, Double> hostHashMap,
			HashMap<String, Double> ipHashMap, HashMap<String, Integer> target, HashMap<String, HashSet<Integer>> resultHashMap, String outfilename) throws IOException {
		// TODO Auto-generated method stub
		
			int count = 0;
			int num = 0;
	        InputStreamReader testread = new InputStreamReader(new FileInputStream(testfile),"GBK");
	        BufferedReader testbufferedReader = new BufferedReader(testread);
	        
	        String testlineTxt = null;
	        while((testlineTxt = testbufferedReader.readLine()) != null){
	        	String target_True = testfile.getName().split("\\.")[0];
//	        	String host = testlineTxt.split(",")[1];
//	        	String useragent = testlineTxt.split(",")[2];
//	        	String ip = testlineTxt.split(",")[3];
	        	
            	String host = StringUtils.substringBetween(testlineTxt,"HOST=[","]");
            	String useragent = StringUtils.substringBetween(testlineTxt,"USER-AGENT=[","]");
            	String ip = StringUtils.substringBetween(testlineTxt,"SERVER-IP=[","]");
            	int fingerprintcount = Integer.valueOf(StringUtils.substringBetween(testlineTxt, "Num=[", "]"));
            	
	        	Iterator<String> iter = target.keySet().iterator();
	        	double prob = -1;
	        	String predict = "";
	        	ArrayList<Double> probsArrayList = new ArrayList<>();
	        	
	        	while(iter.hasNext()){
	        		String targetstring = iter.next();
	        		
	        		double Pos_useragent = 0,Pos_host = 0,Pos_ip = 0;
	        		if(useragentHashMap.keySet().contains(targetstring+","+useragent))
	        			Pos_useragent = useragentHashMap.get(targetstring+","+useragent);
	        		else
	        			Pos_useragent = 0.00001;
	        		
	        		if(hostHashMap.keySet().contains(targetstring+","+host))
	        			Pos_host = hostHashMap.get(targetstring+","+host);
	        		else
	        			Pos_host = 0.00001;
	        		
	        		if(ipHashMap.keySet().contains(targetstring+","+ip))
	        			Pos_ip = ipHashMap.get(targetstring+","+ip);
	        		else
	        			Pos_ip = 0.00001;
	        		
	        		probsArrayList.add(Pos_useragent*Pos_host*Pos_ip*target.get(targetstring));
//	        		Revised here.
//	        		probsArrayList.add(Pos_useragent*Pos_host*Pos_ip);

	        		
//	        		System.out.println("target: "+targetstring +"   pro: "+Pos_useragent*Pos_host*Pos_ip*target.get(targetstring));
	        		
	        		if(Pos_useragent*Pos_host*Pos_ip*target.get(targetstring) > prob){
	        			prob = Pos_useragent*Pos_host*Pos_ip*target.get(targetstring);
	        			predict = targetstring;
	        			
//	        		if(Pos_useragent*Pos_host*Pos_ip> prob){
//	        			prob = Pos_useragent*Pos_host*Pos_ip;
//	        			predict = targetstring;
	        		
//	        		if(Pos_useragent*Pos_host*target.get(targetstring) > prob){
//	        			prob = Pos_useragent*Pos_host*target.get(targetstring);
//	        			predict = targetstring;
	        		}
	        	}
	        	
	        	Collections.sort(probsArrayList);
	        	
	        	if(probsArrayList.get(probsArrayList.size()-1) <= probsArrayList.get(probsArrayList.size()-2) * 2)
	        		predict = "others";
	        	
//	        	if(!predict.equals("weibo"))
//	        	System.out.println(predict+" --- "+prob);
//	        	System.out.println("target_True:"+target_True+" | predict:"+predict);
	        	
	        	if(resultHashMap.keySet().contains(predict)){
	        		HashSet<Integer> hashSet = resultHashMap.get(predict);
	        		String[] nums = StringUtils.substringBetween(testlineTxt,"Set=[","]").split(",");
	        		for(int j=0;j<nums.length;j++)
	        			hashSet.add(String_TO_Int(nums[j]));
	        	}
	        	else{
	        		HashSet<Integer> hashSet = new HashSet<>();
	        		String[] nums = StringUtils.substringBetween(testlineTxt,"Set=[","]").split(",");
	        		for(int j=0;j<nums.length;j++){
	        			hashSet.add(String_TO_Int(nums[j]));
	        		}
	        		resultHashMap.put(predict, hashSet);
	        	}
	        	
	        	if(predict.equals(target_True)) count += fingerprintcount;
	        		num += fingerprintcount;
	        }
			testread.close();
	        
//		������Ҫ�޸ģ��������������ļ���Ϊ�ļ��С�
	        WriteIntoFiles(jFile_AddTestfile.getSelectedFile().getAbsolutePath(),resultHashMap,outfilename);
	        
	        System.out.println("Count:"+count+" Num:"+num);
			return (double)count/num;
	}

	private Integer String_TO_Int(String string) {
		// TODO Auto-generated method stub
		if(string.contains(" "))
			return Integer.parseInt(string.substring(1));
		else
			return Integer.parseInt(string);
	}

	private void WriteIntoFiles(String pcapFile,
			HashMap<String, HashSet<Integer>> resultHashMap, String string) throws IOException {
		// TODO Auto-generated method stub
		File file = new File(string);
		if(!file.exists())
			file.mkdirs();
		File testFile = new File(pcapFile);
		if(!testFile.exists())
			testFile.mkdirs();
		
		
		System.out.println("Writing...");
		
		Iterator<String> iterator = resultHashMap.keySet().iterator();
		while(iterator.hasNext()){
			String keyString = iterator.next();
			String hanyupinyin = converterToSpell(keyString);
			
			System.out.println("Key:"+keyString);
			
			String outFilePath = string+"\\"+hanyupinyin+".pcap";
			
//			System.out.println(keyString+"  "+resultHashMap.get(keyString));
			
			Dump(testFile.getAbsolutePath(),outFilePath,resultHashMap.get(keyString));
		}
	}
	
    public String converterToSpell(String chines){ 	 
        String pinyinName = "";
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    pinyinName += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0];
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            }else{
            	pinyinName += nameChar[i];
            }
        }
        return pinyinName;
	}

		private void Dump(String pcapFile, String outFile,
				final HashSet<Integer> hashSet) throws IOException {
			// TODO Auto-generated method stub
			final String FILENAME = pcapFile;
			final StringBuilder errbuf = new StringBuilder();
			final Pcap pcap = Pcap.openOffline(FILENAME, errbuf);
			if (pcap == null) {
				System.err.println(errbuf);
				return ;
			}
//			System.out.println("outFile:"+outFile);
			File outfileFile = new File(outFile);
			if(!outfileFile.exists())
				outfileFile.createNewFile();
			
			final PcapDumper dumper = pcap.dumpOpen(outFile); 
			
			number = 1;
			pcap.loop(Pcap.LOOP_INFINITE, new JPacketHandler<StringBuilder>() {
	
				@Override
				public void nextPacket(JPacket packet, StringBuilder errbuf) {
					if(hashSet.contains(number)){
						dumper.dump((PcapHeader) packet.getCaptureHeader(), packet);
					}
					number++;
				}

			},errbuf);
			
			pcap.close();
			dumper.close();
		}

	}


	private void update(HashMap<String, Double> HashMap, HashMap<String, Integer> target) {
		// TODO Auto-generated method stub
		/*
		Iterator<String> iterator = HashMap.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			System.out.println(key);
			String targetstring = key.split(",")[0];
			if(!target.containsKey(targetstring))
				System.out.println("Not exists");
			int countall = target.get(targetstring);
			System.out.println(countall);
			double result = HashMap.get(key) / countall;
			HashMap.put(targetstring, result);
		}
		*/
		
		Iterator it_d = HashMap.entrySet().iterator();    
	    while (it_d.hasNext()) {
	    	HashMap.Entry entry_d = (HashMap.Entry) it_d.next();    
	        String key = (String)entry_d.getKey();    
			String targetstring = key.split(",")[0];
			
	        double value = (Double) entry_d.getValue();
	        int countall = target.get(targetstring);
	        
	        value = value / countall;
	        HashMap.put((String)key, (Double)value);
	    }
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
//				System.out.println(tempString);
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
							+ "] SERVER-IP=[" + serverAddr + "] SOURCE-PORT=["
							+ sourcePort + "]";
					Integer id = new Integer(strArray[0]);
					increaseCount(map, resultStr,id);
				}

			}
			double a = ((double) httpLine / line)*100;
			System.out.println(outFile+"   http��ռ"+a+"%");
			
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
//				   System.out.println("key= "+ key + " and Num = " + map.get(key).size() +" id set ="+map.get(key).toString());
//				   System.out.println(" id set ="+map.get(key).toString());

//				   bwer.write("key= "+ key + " and Num = " + map.get(key).size() +" id set ="+map.get(key).toString());
//				   bwer.write(key + " Num=[" + map.get(key).size() +"] Set=[" + writeSet(map.get(key))+"]");
				   bwer.write(key + " Num=[" + map.get(key).size() +"] Set=" + map.get(key).toString());

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

	@SuppressWarnings("unused")
	private void GetHttpNum(String inFile, String outFile) {
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
				HashMap<String, Integer> map = new HashMap<String, Integer>();
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
						System.out.println(tempString);
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
									+ "] SERVER-IP=[" + serverAddr + "] SOURCE-PORT=["
									+ sourcePort + "]";
							increaseCount(map, resultStr);
						}

					}
					double a = ((double) httpLine / line)*100;
					System.out.println("http��ռ"+a+"%");
					
					fwer = new FileWriter(countFile, true);
					bwer = new BufferedWriter(fwer);
					//map���򣬰�Ƶ���ɸߵ���
					ArrayList<Map.Entry<String, Integer>> sorted = new ArrayList<>(
							map.entrySet());

					sorted.sort(new Comparator<Map.Entry<String, Integer>>() {
						@Override
						public int compare(Entry<String, Integer> o1,
								Entry<String, Integer> o2) {
							// TODO Auto-generated method stub
							return -o1.getValue().compareTo(o2.getValue());
						}
					});
					//����mapд�ļ�
					for (Map.Entry<String, Integer> entry : sorted) {
						System.out.println(entry.getKey() + ":" + entry.getValue()+" "+a);
						bwer.write(entry.getKey() + " PacketNums:" +  entry.getValue());
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

	void increaseCount(Map<String, Integer> countMap, String key) {
		if (countMap.containsKey(key)) {
			Integer value = countMap.get(key) + 1;
			countMap.put(key, value);
		} else {
			countMap.put(key, 1);
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
		number = 1;
		
		// �������а����н���
		pcap.loop(Pcap.LOOP_INFINITE, new JPacketHandler<StringBuilder>() {

			// �Ƿ���httpЭ��ͷ��־
			boolean isHttp = false;
			// �Ƿ�Request����
			boolean isRequest = false;
			// �Ƿ�Response����
			@SuppressWarnings("unused")
			boolean isResponse = false;
			// �Ƿ�tcpЭ��
			boolean isTcp = false;
			// �Ƿ�udpЭ��
			@SuppressWarnings("unused")
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
//										bw.write(packet.getFrameNumber()
										bw.write(number
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
//										bw.write(packet.getFrameNumber()
										bw.write(number
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
//							bw.write(packet.getFrameNumber()
							bw.write(number
									+ " "
									+ packet.getCaptureHeader()
											.timestampInMillis() + " "
									+ packet.getPacketWirelen() + " " + sp
									+ " " + dp + " " + false);
							bw.newLine();
						}
					} else {
						// httpGroupΪ�գ�˵����ǰ����������http����
//						bw.write(packet.getFrameNumber() + " "
						bw.write(number 
								+ " "
								+ packet.getCaptureHeader().timestampInMillis()
								+ " " + packet.getPacketWirelen() + " " + sp
								+ " " + dp + " " + false);
						bw.newLine();
					}
					// д�ļ�end
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				number++;
			}

		}, errbuf);
		bw.close();
		// �ر�pcap
		pcap.close();
	}

  }
