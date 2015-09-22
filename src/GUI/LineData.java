package GUI;

import java.util.HashSet;

public class LineData {
	long time;
	HashSet<Integer> port;
	
	public LineData(long _time, HashSet<Integer> _port){
		this.time = _time;
		this.port = _port;
	}
}
