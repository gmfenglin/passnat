package com.feng.lin.pass.nat.server.bean;

import io.netty.channel.Channel;

public class ChannelConfig {
	private String domain;
	private Channel tunnelChannel;
	private Channel channel;

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Channel getTunnelChannel() {
		return tunnelChannel;
	}

	public void setTunnelChannel(Channel tunnelChannel) {
		this.tunnelChannel = tunnelChannel;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

}
