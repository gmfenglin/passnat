package com.feng.lin.pass.nat.client.proxy;

import java.nio.charset.Charset;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;

public class ClientHandler extends ChannelInboundHandlerAdapter {
	private Channel channel;

	public ClientHandler(Channel channel) {
		super();
		this.channel = channel;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpResponse response = (FullHttpResponse) msg;
		System.out.println("channelRead content:" + response.content().toString(Charset.defaultCharset()).length());
		channel.writeAndFlush(response);
	}

}
