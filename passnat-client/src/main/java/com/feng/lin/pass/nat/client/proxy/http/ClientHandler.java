package com.feng.lin.pass.nat.client.proxy.http;

import java.nio.charset.Charset;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;

public class ClientHandler extends SimpleChannelInboundHandler<HttpObject> {
	private Channel channel;
	private String reqId;

	public ClientHandler(String reqId, Channel channel) {
		super(false);
		this.channel = channel;
		this.reqId = reqId;
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
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		System.out.println(msg);
		if (msg instanceof FullHttpResponse) {
			FullHttpResponse response = (FullHttpResponse) msg;
			response.headers().add("reqId", reqId);
			System.out.println("channelRead content:" + response.content().toString(Charset.defaultCharset()).length());
			channel.writeAndFlush(response);
		}

	}

}
