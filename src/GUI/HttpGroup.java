package GUI;

class HttpGroup {
	/**
	 * Դip�˿�
	 **/
	private String sIpPort;
	/**
	 * Ŀ��ip�˿�
	 **/
	private String dIpPort;
	/**
	 * ʹ�ñ�־
	 **/
	private boolean useFlag;
	/**
	 * httpЭ��host�ֶ�
	 **/
	private String hostStr;
	/**
	 * httpЭ��user_agent�ֶ�
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

