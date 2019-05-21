package com.feng.lin.pass.nat.client.bean;

import java.io.Serializable;
import java.util.List;

public class Config implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6654827544381045510L;
	private String serverAddr;
	private int port;
	private String domain;
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	private boolean trustHostRootCerts;
	private List<Tunel> tunels;
	public String getServerAddr() {
		return serverAddr;
	}
	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}
	public boolean isTrustHostRootCerts() {
		return trustHostRootCerts;
	}
	public void setTrustHostRootCerts(boolean trustHostRootCerts) {
		this.trustHostRootCerts = trustHostRootCerts;
	}
	public List<Tunel> getTunels() {
		return tunels;
	}
	public void setTunels(List<Tunel> tunels) {
		this.tunels = tunels;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	@Override
	public String toString() {
		return "Config [serverAddr=" + serverAddr + ", port=" + port + ", domain=" + domain + ", trustHostRootCerts="
				+ trustHostRootCerts + ", tunels=" + tunels + "]";
	}
	
}
