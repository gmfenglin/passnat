package com.feng.lin.pass.nat.server.bean;

public class Config {
	private String domain;
	private int httpAddr = 80;
	private int httpsAddr = 443;
	private int tunelAddr = 4443;
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public int getHttpAddr() {
		return httpAddr;
	}
	public void setHttpAddr(int httpAddr) {
		this.httpAddr = httpAddr;
	}
	public int getHttpsAddr() {
		return httpsAddr;
	}
	public void setHttpsAddr(int httpsAddr) {
		this.httpsAddr = httpsAddr;
	}
	public int getTunelAddr() {
		return tunelAddr;
	}
	public void setTunelAddr(int tunelAddr) {
		this.tunelAddr = tunelAddr;
	}
	@Override
	public String toString() {
		return "Config [domain=" + domain + ", httpAddr=" + httpAddr + ", httpsAddr=" + httpsAddr + ", tunelAddr="
				+ tunelAddr + "]";
	}
	
}
