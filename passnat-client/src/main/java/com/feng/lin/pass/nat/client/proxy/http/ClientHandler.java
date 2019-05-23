package com.feng.lin.pass.nat.client.proxy.http;

import java.nio.charset.Charset;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;

public class ClientHandler extends ChannelInboundHandlerAdapter {
	private Channel channel;
	private String reqId;

	public ClientHandler(String reqId, Channel channel) {
		super();
		this.channel = channel;
		this.reqId = reqId;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("exceptionCaught:"+cause.getMessage());
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpResponse response = (FullHttpResponse) msg;
		response.headers().add("reqId", reqId);
		System.out.println("channelRead content:" + response.content().toString(Charset.defaultCharset()).length());
		channel.writeAndFlush(response);
	}

}
