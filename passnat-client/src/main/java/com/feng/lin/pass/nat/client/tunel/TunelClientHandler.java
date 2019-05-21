package com.feng.lin.pass.nat.client.tunel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feng.lin.pass.nat.client.proxy.Client;
import com.feng.lin.pass.nat.comm.debug.Loger;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;

public class TunelClientHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static final Logger logger = LoggerFactory.getLogger(TunelClientHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		super.exceptionCaught(ctx, cause);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		Loger.debugLog(logger, () -> "channelRead0 TunelClientHandler: " + msg);
		Channel channel = ctx.channel();
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest request = (FullHttpRequest) msg;
			Loger.debugLog(logger, () -> "call proxy: " + request.uri());
			Client.start("192.168.1.101", 9001, (FullHttpRequest) msg, ctx.channel());
		} else if (msg instanceof FullHttpResponse) {

		}
	}

}
