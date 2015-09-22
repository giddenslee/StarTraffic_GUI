package GUI;

public class TimeInterval {
	String appid;
	long StartTime;
	long EndTime;
	
	public TimeInterval(String _appid,long _starttime,long _endtime){
		this.appid = _appid;
		this.StartTime = _starttime;
		this.EndTime = _endtime;
	}
}
