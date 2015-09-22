package GUI;

class HttpGroup {
	/**
	 * 源ip端口
	 **/
	private String sIpPort;
	/**
	 * 目的ip端口
	 **/
	private String dIpPort;
	/**
	 * 使用标志
	 **/
	private boolean useFlag;
	/**
	 * http协议host字段
	 **/
	private String hostStr;
	/**
	 * http协议user_agent字段
	 **/
	private String UserAgentStr;

	public String getHostStr() {
		return this.hostStr;
	}

	public void setHostStr(String hostStr) {
		this.hostStr = hostStr;
	}

	public String getUserAgentStr() {
		return this.UserAgentStr;
	}

	public void setUserAgentStr(String userAgentStr) {
		this.UserAgentStr = userAgentStr;
	}

	public HttpGroup() {
		this.sIpPort = "";
		this.dIpPort = "";
		this.useFlag = false;
		this.hostStr = "";
		this.UserAgentStr = "";
	}

	public boolean isUseFlag() {
		return this.useFlag;
	}

	public void setUseFlag(boolean useFlag) {
		this.useFlag = useFlag;
	}

	public String getsIpPort() {
		return this.sIpPort;
	}

	public void setsIpPort(String sIpPort) {
		this.sIpPort = sIpPort;
	}

	public String getdIpPort() {
		return this.dIpPort;
	}

	public void setdIpPort(String dIpPort) {
		this.dIpPort = dIpPort;
	}

}

