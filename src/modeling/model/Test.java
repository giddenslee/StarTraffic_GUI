package modeling.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import modeling.model.traffic.TrafficModel;
import modeling.model.traffic.TrafficModelSession;
import modeling.model.traffic.TrafficModelPacket;
import modeling.packet.PacketInfo;
import modeling.packet.PacketCollector;
import modeling.tools.TextInOut;

public class Test {
	public static void main(String[] args) throws IOException {
		PacketCollector collector = new PacketCollector();
		ArrayList<PacketInfo> packets = collector.collect("WSexport/weixinTcp.txt");
		
		packets = collector.fliterRetransmission(packets);
		
		System.out.println("Original Packets:"+packets.size());
		
		TrafficModel trafficModel = new TrafficModelPacket(25, 25);
		//TrafficModel trafficModel = new TrafficModelFlow(1460 , 10);
		trafficModel.buildModelFromPackets(packets);
		
		System.out.println("Model Parameter Number:"+trafficModel.getParameterNumber());
		
		ArrayList<String> outputs = new ArrayList<String>();
		outputs.add(trafficModel.toString());
		TextInOut.writeFile(outputs, "weixin.flow.model");
		outputs = new ArrayList<String>();
		outputs.add(trafficModel.toPrettyString());
		TextInOut.writeFile(outputs, "weixin.flow.model.formatted");
		
		System.out.println("model built");
		
		List<PacketInfo> generated = trafficModel.generatePackets(860);
		
		PacketCollector.WriteToFiles(generated, "weixin_flow");
		
		System.out.println("Generated Packets:"+generated.size());
		
		System.out.println(TrafficModel.fromJson(trafficModel.toString()));
		
	}
}
