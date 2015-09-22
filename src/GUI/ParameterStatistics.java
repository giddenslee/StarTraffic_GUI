package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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

public class ParameterStatistics extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8663325580222779633L;
	private PathField pathModel;
	private PathField pathOutput;

	private JLabel ParaLabel;
	
	private JTextField txtPC;
	private JTextField txtPN;

	private JButton btnStart;

	private JPanel panShow;

	private List<PacketInfo> latestPackets;

	public ParameterStatistics() {
		this.setLayout(null);

		JLabel lbl1 = new JLabel("参数统计");
		lbl1.setBounds(220, 50, 100, 30);
		this.add(lbl1);

		pathModel = new PathField(true, false, new ExtensionFileFilter(
				"生成流量文件", "gen"));
		pathModel.setPath(Setting.getWorkspacePath() + "generated");
		pathModel.setBounds(400, 50, 450, 30);
		this.add(pathModel);
		
		JLabel lbl2 = new JLabel("输出路径");
		lbl2.setBounds(220, 100, 100, 30);
		this.add(lbl2);

		pathOutput = new PathField(false, true, null);
		pathOutput.setPath(Setting.getWorkspacePath() + "generated");
		pathOutput.setBounds(400, 100, 450, 30);
		this.add(pathOutput);

		ParaLabel = new JLabel("参数设置");
		ParaLabel.setBounds(220, 150, 100, 30);
		this.add(ParaLabel);		
		
		JLabel lbl4 = new JLabel("PS Call响应阈值:");
		lbl4.setBounds(220, 200, 200, 30);
		this.add(lbl4);
		
		txtPC = new JTextField("10");
		txtPC.setBounds(450, 200, 100, 30);
		this.add(txtPC);

		JLabel lbl5 = new JLabel("秒");
		lbl5.setBounds(620, 200, 150, 30);
		this.add(lbl5);
		
		JLabel lbl6 = new JLabel("Paging Number响应阈值:");
		lbl6.setBounds(220, 250, 200, 30);
		this.add(lbl6);
		
		txtPN = new JTextField("10");
		txtPN.setBounds(450, 250, 100, 30);
		this.add(txtPN);

		JLabel lbl7 = new JLabel("秒");
		lbl7.setBounds(620, 250, 100, 30);
		this.add(lbl7);

		JButton button1 = new JButton("开始统计");
		button1.setBounds(900, 50, 150, 30);
		button1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO 加入各参数统计并输出结果
			}
		});
		this.add(button1);

		panShow = new JPanel();
		panShow.setBounds(0, 300, 1200, 500);
		panShow.setBackground(Color.WHITE);
		this.add(panShow);

	}
}
