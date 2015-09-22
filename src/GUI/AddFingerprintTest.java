package GUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.sound.sampled.Line;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapDumper;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

public class AddFingerprintTest {
	public AddFingerprintTest(String txtfilenameString,
			String pcapfilenameString, String pcapfilterd, HashMap<String, String> nameHashMap) throws IOException {
		// TODO Auto-generated constructor stub
		
		HashMap<String, ArrayList<LineData>> hashMap = new HashMap<>();
		FetchDataIntoHashMap(txtfilenameString,hashMap,nameHashMap);
		
		PrintHashMap(hashMap,false);
		
		HashMap<Integer, ArrayList<TimeInterval>> searchHashMap = new HashMap<>();
		Reconsitution(hashMap,searchHashMap);
		
		PrintSearchHashMap(searchHashMap,false);
		
		MergeTimeInterval(searchHashMap);
		
		PrintSearchHashMap(searchHashMap, false);
		
		DumpIntoFiles(pcapfilenameString,searchHashMap,pcapfilterd);
		
	}



	private static void DumpIntoFiles(String pcapfilenameString,
			final HashMap<Integer, ArrayList<TimeInterval>> searchHashMap,
			final String pcapfilterd) throws IOException {
		// TODO Auto-generated method stub
		
			final String FILENAME = pcapfilenameString;
			final StringBuilder errbuf = new StringBuilder();
			final Pcap pcap = Pcap.openOffline(FILENAME, errbuf);
			if (pcap == null) {
				System.err.println(errbuf);
				return ;
			}
			
			final HashMap<String, PcapDumper> dumperHashMap = new HashMap<>();
			
			pcap.loop(Pcap.LOOP_INFINITE, new JPacketHandler<StringBuilder>() {
				
				final Tcp tcp = new Tcp();
				final Udp udp = new Udp();

				@Override
				public void nextPacket(JPacket packet, StringBuilder errbuf) {
					
					int sPort = 0;
					int dPort = 0;
					if (packet.hasHeader(this.tcp)) {
						sPort = this.tcp.source();
						dPort = this.tcp.destination();
					}
					if (packet.hasHeader(this.udp)) {
						sPort = this.udp.source();
						dPort = this.udp.destination();
					} 
					
					String appidString = MatchData(searchHashMap,sPort,dPort,packet.getCaptureHeader().timestampInMillis());
//					System.out.println(appidString);
					if(!appidString.equals("NothingFind")){
						if(dumperHashMap.containsKey(appidString)){
							dumperHashMap.get(appidString).dump((PcapHeader) packet.getCaptureHeader(), packet);
						}
						else{
							PcapDumper dumper = pcap.dumpOpen(pcapfilterd+"\\"+appidString+".pcap");					
							dumper.dump((PcapHeader) packet.getCaptureHeader(), packet);
							dumperHashMap.put(appidString, dumper);
						}
//						System.out.println(sPort+" --- > "+dPort);
//						System.out.println(packet.getCaptureHeader().timestampInMillis());
					}
				}

				private String MatchData(
						HashMap<Integer, ArrayList<TimeInterval>> searchHashMap,
						int sPort, int dPort, long timestampInMillis) {
					// TODO Auto-generated method stub
					if(searchHashMap.containsKey(sPort)){
						ArrayList<TimeInterval> arrayList = searchHashMap.get(sPort);
						for(int i=0;i<arrayList.size();++i){
							if(arrayList.get(i).StartTime <= timestampInMillis && arrayList.get(i).EndTime >= timestampInMillis)
								return arrayList.get(i).appid;
						}
					}
					
					else if(searchHashMap.containsKey(dPort)){
						ArrayList<TimeInterval> arrayList = searchHashMap.get(dPort);
						for(int i=0;i<arrayList.size();++i){
							if(arrayList.get(i).StartTime <= timestampInMillis && arrayList.get(i).EndTime >= timestampInMillis)
								return arrayList.get(i).appid;
						}
					}
					return "NothingFind";
				}

			},errbuf);
			pcap.close();
			
			Iterator<String> dumpIterator = dumperHashMap.keySet().iterator();
			while(dumpIterator.hasNext()){
				dumperHashMap.get(dumpIterator.next()).close();
			}
		}

	private static void PrintSearchHashMap(
			HashMap<Integer, ArrayList<TimeInterval>> searchHashMap, boolean OnOff) {
		// TODO Auto-generated method stub
		if(OnOff){
			System.out.println("   ------ SearchHashMap ------");
			Iterator<Integer> iterator = searchHashMap.keySet().iterator();
			while(iterator.hasNext()){
				int port = iterator.next();
				System.out.println(" --- Port "+port+" ---");
				ArrayList<TimeInterval> arrayList = searchHashMap.get(port);
				for(int i=0;i<arrayList.size();++i){
					System.out.println(arrayList.get(i).appid +" " +arrayList.get(i).StartTime +" "+arrayList.get(i).EndTime);
				}
			}
		}
	}


	private static void Reconsitution(
			HashMap<String, ArrayList<LineData>> hashMap,
			HashMap<Integer, ArrayList<TimeInterval>> searchHashMap) {
		// TODO Auto-generated method stub
		Iterator<String> iterator = hashMap.keySet().iterator();
		while(iterator.hasNext()){
			String appid = iterator.next();
			ArrayList<LineData> arrayList = hashMap.get(appid);
			for(int i=0;i<arrayList.size();++i){
				LineData currentLineData = arrayList.get(i);
				long currentTime = currentLineData.time;
				long nextTime;
				if(i+1 < arrayList.size())
					nextTime = arrayList.get(i+1).time;
				else
					nextTime = Long.MAX_VALUE;
				HashSet<Integer> currentSet = currentLineData.port;
				Iterator<Integer> iteratorSet = currentSet.iterator();
				while(iteratorSet.hasNext()){
					int port = iteratorSet.next();
					if(searchHashMap.containsKey(port)){
						ArrayList<TimeInterval> SearchArrayList = searchHashMap.get(port);
						TimeInterval timeInterval = new TimeInterval(appid, currentTime, nextTime);
						SearchArrayList.add(timeInterval);
					}
					else{
						ArrayList<TimeInterval> SearchArrayList = new ArrayList<>();
						TimeInterval timeInterval = new TimeInterval(appid, currentTime, nextTime);
						SearchArrayList.add(timeInterval);
						searchHashMap.put(port, SearchArrayList);
					}
				}
			}
		}

	}


	private static void MergeTimeInterval(
			HashMap<Integer, ArrayList<TimeInterval>> searchHashMap) {
		// TODO Auto-generated method stub
		Iterator<Integer> iterator = searchHashMap.keySet().iterator();
		while(iterator.hasNext()){
			int port = iterator.next();
			ArrayList<TimeInterval> arrayList = searchHashMap.get(port);
			for(int i = arrayList.size()-1;i>0;--i){
				TimeInterval currentInterval = arrayList.get(i);
				TimeInterval lastInterval = arrayList.get(i-1);
				if(currentInterval.StartTime == lastInterval.EndTime){
					lastInterval.EndTime = currentInterval.EndTime;
					arrayList.remove(i);
				}
			}
		}
	}


	private static void PrintHashMap(
			HashMap<String, ArrayList<LineData>> hashMap, boolean OnOff) {
		// TODO Auto-generated method stub
		if(OnOff){
			Iterator<String> iterator = hashMap.keySet().iterator();
			while(iterator.hasNext()){
				String keyString = iterator.next();
				ArrayList<LineData> arrayList = hashMap.get(keyString);
				
				System.out.println(" --- " + keyString+" --- ");
				for(int i=0;i<arrayList.size();++i){
					System.out.println("Time: "+arrayList.get(i).time);
					HashSet<Integer> hashSet = arrayList.get(i).port;
					Iterator<Integer> setIterator = hashSet.iterator();
					while(setIterator.hasNext()){
						System.out.print(setIterator.next()+" ");
					}
					System.out.println();
				}
			}
		}
	}


	private static void FetchDataIntoHashMap(String txtfilenameString,
			HashMap<String, ArrayList<LineData>> hashMap, HashMap<String, String> nameHashMap) throws IOException {
		// TODO Auto-generated method stub
		
        InputStreamReader read = new InputStreamReader(new FileInputStream(new File(txtfilenameString)),"UTF-8");
        BufferedReader br = new BufferedReader(read);
        String lineString = br.readLine();
        
        File nameFile = new File(Setting.getWorkspacePath()+"fingerprint\\.temp\\nametemp\\name.txt");
        if(!nameFile.exists()){
        	nameFile.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(nameFile,true));
        String bufferString="";
        int appcount = Integer.valueOf(lineString.split(" ")[3]);
        for(int i=0;i<appcount;++i){
        	lineString = br.readLine();
        	
        	nameHashMap.put(lineString.split(" ")[1], lineString.split(" ")[0]);

        	bufferString += lineString+"\n";
        }
        writer.write(bufferString);
        writer.close();
        
        br.readLine();
        while((lineString = br.readLine()) != null){
        	
        	String appid = lineString.split("[ ]+")[0];
        	
        	long time = Long.valueOf(lineString.split("[ ]+")[1]);
        	HashSet<Integer> ports = new HashSet<>();
        	for(int i=2;i<lineString.split("[ ]+").length;++i)
        		ports.add(Integer.valueOf(lineString.split("[ ]+")[i]));
        	
        	LineData linedata = new LineData(time, ports);
        	
        	if(hashMap.containsKey(appid))
        		hashMap.get(appid).add(linedata);
        	else{
        		ArrayList<LineData> arrayList = new ArrayList<>();
        		arrayList.add(linedata);
        		hashMap.put(appid, arrayList);
        	}
        }
        br.close();
	}
}
