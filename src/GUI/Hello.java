package GUI;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
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


public class Hello{
	public static void main(String args[])throws Exception{
		Setting.checkFistRun();
		
		MainFrame frame = new MainFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}
}

class MainFrame extends JFrame{
	
	private JTabbedPane tabbedPane = null;
	
	public MainFrame(){
	   tabbedPane = new JTabbedPane();
	   
	   tabbedPane.setFont(new Font("΢���ź�",Font.PLAIN,20));
	   tabbedPane.addTab("ָ�ƹ���", new FingerprintManagement());
	   tabbedPane.addTab("��������", new TrafficClassification());
	   tabbedPane.addTab("������ģ", new TrafficModeling());
	   tabbedPane.addTab("��������", new TrafficGeneration());
	   tabbedPane.addTab("����", new Setting());
/*+2*/ 
	   tabbedPane.addTab("�����ۺ�", new TrafficCombound());
	   tabbedPane.addTab("����ͳ��", new ParameterStatistics());

	   add(tabbedPane, "Center");
	   
	   this.setTitle("��������ͽ�ģ����");
	   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	   screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	   this.setPreferredSize(new Dimension(1200,800));            
	   int frameWidth = this.getPreferredSize().width;
	   int frameHeight = this.getPreferredSize().height;
	   this.setSize(frameWidth, frameHeight);
	   this.setLocation((screenSize.width - frameWidth) / 2,(screenSize.height - frameHeight) / 2);
	   this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
