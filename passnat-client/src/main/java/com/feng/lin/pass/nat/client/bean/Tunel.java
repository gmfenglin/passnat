package com.feng.lin.pass.nat.client.bean;

public class Tunel {
	private String name;
	private String subDomain;
	private String proto;
	private String host;
	private String schema;
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	private int port;
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubDomain() {
		return subDomain;
	}
	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}
	public String getProto() {
		return proto;
	}
	public void setProto(String proto) {
		this.proto = proto;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	@Override
	public String toString() {
		return "Tunel [name=" + name + ", subDomain=" + subDomain + ", proto=" + proto + ", host=" + host + ", schema="
				+ schema + ", port=" + port + "]";
	}
	
}
