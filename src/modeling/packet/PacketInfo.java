package modeling.packet;

import java.util.Set;

public class PacketInfo implements Comparable<PacketInfo> {
	public final static int FLAG_FIN = 1;
	public final static int FLAG_SYN = 2;
	public final static int FLAG_ACK = 16;

	public int id;
	public long time;// InMicros
	public String sourceIp;
	public String destIp;
	public String protocol;
	public int length;
	public long seqNum;
	public long ackNum;
	public String info;

	public String serverIp;
	public boolean upload;
	public int localPort;
	public int remotePort;
	public int flags;

	public PacketInfo() {
	};

	public PacketInfo(long time, int length, boolean upload) {
		this.time = time;
		this.length = length;
		this.upload = upload;
	}

	public static PacketInfo praseTCPInfoFromWireshark(String info) {
		if (info.startsWith("\"No."))
			return null;
		PacketInfo result = new PacketInfo();
		info = info.replaceAll("\"", "");
		String[] parts = info.split(",");

		if (parts.length > 0)
			result.id = Integer.parseInt(parts[0]);
		if (parts.length > 1)
			result.time = Long.parseLong(parts[1].replaceAll("\\.", ""));
		if (parts.length > 2)
			result.sourceIp = parts[2];
		if (parts.length > 3)
			result.destIp = parts[3];
		if (parts.length > 4)
			result.protocol = parts[4];
		if (parts.length > 5)
			result.length = Integer.parseInt(parts[5]);
		if (parts.length > 6) {
			result.info = parts[6];
			if (parts.length > 7)
				result.info += "," + parts[7];
		}
		if ("TCP".equals(result.protocol)) {
			parts = result.info.split(" ");
			if (parts.length >= 4) {
				String flagstr = "";
				if (">".equals(parts[1])) {
					result.localPort = Integer.parseInt(parts[0]);
					result.remotePort = Integer.parseInt(parts[2]);
					flagstr = parts[3];
				} else if (">".equals(parts[2])) {
					result.localPort = Integer.parseInt(parts[1]);
					result.remotePort = Integer.parseInt(parts[3]);
					flagstr = parts[4];
				}

				if (flagstr.contains("SYN"))
					result.flags |= FLAG_SYN;
				if (flagstr.contains("FIN"))
					result.flags |= FLAG_FIN;
				if (flagstr.contains("ACK"))
					result.flags |= FLAG_ACK;
			}
		}
		return result;
	}

	public static PacketInfo praseTCPInfo(String info) {
		if (info.startsWith("\"No."))
			return null;
		PacketInfo result = new PacketInfo();
		info = info.replaceAll("\"", "");
		String[] parts = info.split(",");

		try {
			if (parts.length > 0)
				result.id = Integer.parseInt(parts[0]);
			if (parts.length > 1)
				result.time = Long.parseLong(parts[1]);
			if (parts.length > 2)
				result.sourceIp = parts[2];
			if (parts.length > 3)
				result.destIp = parts[3];
			if (parts.length > 4)
				result.protocol = parts[4];
			if (parts.length > 5)
				result.length = Integer.parseInt(parts[5]);
			if (parts.length > 6) {
				// result.info = parts[6];
				String[] tmp = parts[6].split(">");
				result.localPort = Integer.parseInt(tmp[0]);
				result.remotePort = Integer.parseInt(tmp[1]);
			}
			for (int i = 7; i <= 9; ++i) {
				if (parts.length > i) {
					if (parts[i].equals("ACK_FLAG:true"))
						result.flags |= FLAG_ACK;
					if (parts[i].equals("SYN_FLAG:true"))
						result.flags |= FLAG_SYN;
					if (parts[i].equals("FIN_FLAG:true"))
						result.flags |= FLAG_FIN;
				}
			}
			if (parts.length>10)
			{
				result.seqNum = Long.parseLong(parts[10]);
			}
			if (parts.length>11)
			{
				result.ackNum = Long.parseLong(parts[11]);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}
	
	public static PacketInfo fillUpTCPInfo(PacketInfo info, Set<String> localIpSet) {
		PacketInfo result = info;
		if (localIpSet.contains(result.sourceIp)) {
			result.serverIp = result.destIp;
			result.upload = true;
		} else if (localIpSet.contains(result.destIp)) {
			result.serverIp = result.sourceIp;
			result.upload = false;
			
			int t = result.localPort;
			result.localPort = result.remotePort;
			result.remotePort = t;
		} else
			return null;
		return result;
	}

	@Override
	public int compareTo(PacketInfo arg0) {
		// TODO Auto-generated method stub
		return Long.compare(time, arg0.time);
	}

	@Override
	public String toString() {
		return "" + time + "," + length + "," + upload;
	}
	
	public double getTimeSecond()
	{
		return time*1e-6;
	}
}
