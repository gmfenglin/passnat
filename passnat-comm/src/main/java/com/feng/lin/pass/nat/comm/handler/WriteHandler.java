package com.feng.lin.pass.nat.comm.handler;

import java.net.SocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class WriteHandler extends ChannelOutboundHandlerAdapter {

	@Override
	public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
		// TODO Auto-generated method stub
		super.bind(ctx, localAddress, promise);
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
			ChannelPromise promise) throws Exception {
		// TODO Auto-generated method stub
		super.connect(ctx, remoteAddress, localAddress, promise);
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		// TODO Auto-generated method stub
		super.disconnect(ctx, promise);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		// TODO Auto-generated method stub
		super.close(ctx, promise);
	}

	@Override
	public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		// TODO Auto-generated method stub
		super.deregister(ctx, promise);
	}

	@Override
	public void read(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.read(ctx);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof FullHttpResponse) {
			if (ctx.pipeline().get("Encoder") != null) {
				ctx.pipeline().replace(ctx.pipeline().get("Encoder"), "Encoder", new HttpResponseEncoder());
			} else {
				ctx.pipeline().addBefore("writeHandler", "Encoder", new HttpResponseEncoder());
			}
			if (ctx.pipeline().get("HttpObjectAggregator") == null) {
				ctx.pipeline().addAfter("Encoder", "HttpObjectAggregator",
						new HttpObjectAggregator(ReadHandler.MAX_LENGTH));
			}

		} else if (msg instanceof FullHttpRequest) {
			if (ctx.pipeline().get("Encoder") != null) {
				ctx.pipeline().replace(ctx.pipeline().get("Encoder"), "Encoder", new HttpRequestEncoder());
			} else {
				ctx.pipeline().addBefore("writeHandler", "Encoder", new HttpRequestEncoder());
			}
			if (ctx.pipeline().get("HttpObjectAggregator") == null) {
				ctx.pipeline().addAfter("Encoder", "HttpObjectAggregator",
						new HttpObjectAggregator(ReadHandler.MAX_LENGTH));
			}
		} else {
			if (ctx.pipeline().get("HttpObjectAggregator") != null) {
				ctx.pipeline().remove("HttpObjectAggregator");
			}
		}
		super.write(ctx, msg, promise);
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.flush(ctx);
	}

}
