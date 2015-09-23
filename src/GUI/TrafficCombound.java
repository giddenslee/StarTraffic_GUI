package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.parser.Entity;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ExtensionFileFilter;

import modeling.model.traffic.TrafficModel;
import modeling.packet.PacketCollector;
import modeling.packet.PacketInfo;
import modeling.tools.TextInOut;

public class TrafficCombound extends JPanel {

	private static final long serialVersionUID = -8663325580222779633L;
	private PathField pathModel1;
	private PathField pathModel2;

	private PathField pathOutput;
	
//	private JLabel ParaLabel;
//	
//	private JTextField txtPC;
//	private JTextField txtPN;

	private JButton btnStart;

	private JPanel panShow;

//	private JTextArea rstArea;
	
	private List<PacketInfo> latestPackets;

	public TrafficCombound() {
		this.setLayout(null);

		JLabel lbl1 = new JLabel("输入路径1");
		lbl1.setBounds(220, 50, 100, 30);
		this.add(lbl1);

		pathModel1 = new PathField(true, false, new ExtensionFileFilter(
				"生成流量文件", "gen"));
		pathModel1.setPath(Setting.getWorkspacePath() + "generated");
		pathModel1.setBounds(400, 50, 450, 30);
		this.add(pathModel1);
		
		JLabel lbl2 = new JLabel("输入路径2");
		lbl2.setBounds(220, 100, 100, 30);
		this.add(lbl2);

		pathModel2 = new PathField(true, false, new ExtensionFileFilter(
				"生成流量文件", "gen"));
		pathModel2.setPath(Setting.getWorkspacePath() + "generated");
		pathModel2.setBounds(400, 100, 450, 30);
		this.add(pathModel2);

		JLabel lbl3 = new JLabel("输出路径");
		lbl3.setBounds(220,150,100,30);
		this.add(lbl3);
		
		pathOutput = new PathField(false,true,null);
		pathOutput.setPath(Setting.getWorkspacePath()+"generated");
		pathOutput.setBounds(400,150,100,30);
		this.add(pathOutput);
		
		
//		ParaLabel = new JLabel("参数设置");
//		ParaLabel.setBounds(220, 150, 100, 30);
//		this.add(ParaLabel);		
		
//		JLabel lbl4 = new JLabel("PS Call响应阈值:");
//		lbl4.setBounds(220, 200, 200, 30);
//		this.add(lbl4);
//		
//		txtPC = new JTextField("10");
//		txtPC.setBounds(450, 200, 100, 30);
//		this.add(txtPC);
//
//		JLabel lbl5 = new JLabel("秒");
//		lbl5.setBounds(620, 200, 150, 30);
//		this.add(lbl5);
		
//		JLabel lbl6 = new JLabel("Paging Number响应阈值:");
//		lbl6.setBounds(220, 250, 200, 30);
//		this.add(lbl6);
//		
//		txtPN = new JTextField("10");
//		txtPN.setBounds(450, 250, 100, 30);
//		this.add(txtPN);
//
//		JLabel lbl7 = new JLabel("秒");
//		lbl7.setBounds(620, 250, 100, 30);
//		this.add(lbl7);

		JButton button1 = new JButton("开始统计");
		button1.setBounds(900, 50, 150, 30);
		button1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO 加入各参数统计并输出结果
				TrafficMix();
			}
		});
		this.add(button1);

//		panShow = new JPanel();
//		panShow.setBounds(0, 300, 1200, 500);
//		panShow.setBackground(Color.WHITE);
//		this.add(panShow);
		
//		rstArea = new JTextArea();
//		rstArea.setBounds(220,350,400,200);
//		rstArea.setLineWrap(true);
//		this.add(rstArea);
		
//		StringBuffer jta = new StringBuffer();
//		ArrayList af = new ArrayList();
//		JFileChooser addChooser=new JFileChooser(".");
//        addChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); 
//        //该方法设置为true允许选择多个文件
//        addChooser.setMultiSelectionEnabled(true); 
//        int returnval=addChooser.showOpenDialog(this);   
//        if(returnval==JFileChooser.APPROVE_OPTION)
//        { 
//            File[] files=addChooser.getSelectedFiles(); 
//            String str=""; 
//            for (File file : files) { 	//等价于for (int i=0;i<files.length;i++)
//                af.add(file); 
//                if(file.isDirectory()) 
//                    str=file.getPath(); 
//                else{ 
//                  str=file.getPath()+file.getName(); 
//                } 
//                jta.append(str+"\n"); 
//                 
//            } 
//        } 
         
    }

	
	private void TrafficMix() {
		File input1 = new File(pathModel1.getPath());
		File input2 = new File(pathModel2.getPath());
		File output = new File(pathOutput.getPath()+".stat");
		
		if (!input1.exists()) {
			JOptionPane.showMessageDialog(this, "输入流量文件1不存在");
			return;
		} else {
			if (!input1.getName().endsWith(".gen")) {
				JOptionPane.showMessageDialog(this, "输入1不是一个流量模拟文件");
				return;
			}
		}
		
		if (!input2.exists()) {
			JOptionPane.showMessageDialog(this, "输入流量文件2不存在");
			return;
		} else {
			if (!input2.getName().endsWith(".gen")) {
				JOptionPane.showMessageDialog(this, "输入2不是一个流量模拟文件");
				return;
			}
		}
		
		if (!output.exists()) {
			try {
				output.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		StringBuffer str = new StringBuffer("");
		try {
			FileReader fr1 = new FileReader(input1);
			FileReader fr2 = new FileReader(input2);
			int ch1 = 0,ch2 = 0;
			while ((ch1 = fr1.read())!=-1 || (ch2 = fr2.read())!=-1)
			{
				if ((char)ch1 != '\0'&& (char)ch1 != '\n' && (char)ch1 != '\r') str.append((char)ch1);
				else {
					int sp1 = str.indexOf(","),sp2 = str.lastIndexOf(",");
					String Time = str.substring(0,sp1-1),Size = str.substring(sp1+1,sp2),isUL = str.substring(sp2+1);
					double time = Double.parseDouble(Time);
					int size = Integer.parseInt(Size);
					if (isUL.equals("true")) 
					{
//						UL+=size;
//						if ((time-lastTime)>PC_lim) PC_count++;
//						if ((time-lastULTime)>PC_lim) PC_count++;
//						lastULTime = time;
					}
					else 
					{
//						DL+=size;
//						if ((time-lastTime)>PN_lim) PN_count++;
//						if ((time-lastDLTime)>PN_lim) PN_count++;
//						lastDLTime = time;
					}
//					lastTime = time;
					str.delete(0, str.length());
				}
			}
			fr1.close();
		} catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("统计文件出错");
		}
		
//		rstArea.setText("PS Call Number:	"+Integer.toString(PC_count)+"\n"
//		+ "Paging Number:	"+Integer.toString(PN_count)+"\n"
//		+ "Upload Packet Size:	"+Double.toString(UL)+"\n"
//		+ "Download Packet Size:	"+Double.toString(DL)+"\n");
		
//		try {
//			FileWriter fw = new FileWriter(output);
//			fw.write("PS Call Number:	"+Integer.toString(PC_count)+"\r\n"
//		+ "Paging Number:	"+Integer.toString(PN_count)+"\r\n"
//		+ "Upload Packet Size:	"+Double.toString(UL)+"\r\n"
//		+ "Download Packet Size:	"+Double.toString(DL)+"\r\n" );
//			fw.close();
//		}catch (IOException e)
//		{
//			e.printStackTrace();
//			System.out.println("输出结果出错");
//		}
		
	}
}
