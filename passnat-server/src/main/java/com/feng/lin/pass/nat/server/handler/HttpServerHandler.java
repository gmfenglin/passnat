package com.feng.lin.pass.nat.server.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.AttributeKey;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	private boolean supportSsl;

	public HttpServerHandler(boolean supportSsl) {
		super();
		this.supportSsl = supportSsl;
	}

	static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest req = (FullHttpRequest) msg;
			req.headers().set("protocol", supportSsl ? "https" : "http");
			String host = req.headers().get("host");
			Optional<String> channelKey = Optional.ofNullable(TunelServerHandler.domainMap.get(host));
			if (!channelKey.isPresent()) {
				FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
						Unpooled.wrappedBuffer(("no tunnel is name " + host).getBytes()));
				response.headers().set(CONTENT_TYPE, "text/plain");
				response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

				response.headers().set(CONNECTION, HttpHeaderValues.CLOSE);
				ctx.writeAndFlush(response);
				return;
			}

			Optional<Channel> channelOptional = Optional
					.ofNullable(TunelServerHandler.channelMap.get(channelKey.get()));
			if (!channelOptional.isPresent()) {
				FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
						Unpooled.wrappedBuffer(("no tunnel client is closed " + host).getBytes()));
				response.headers().set(CONTENT_TYPE, "text/plain");
				response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

				response.headers().set(CONNECTION, HttpHeaderValues.CLOSE);
				ctx.writeAndFlush(response);
				return;
			}
			Channel channel = channelOptional.get();
			if (!channel.isActive()) {
				FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
						Unpooled.wrappedBuffer(("no tunnel client is inactive " + host).getBytes()));
				response.headers().set(CONTENT_TYPE, "text/plain");
				response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

				response.headers().set(CONNECTION, HttpHeaderValues.CLOSE);
				ctx.writeAndFlush(response);
				return;
			}
			String reqId = UUID.randomUUID().toString();
			req.headers().set("reqId", reqId);
			AttributeKey<Channel> keyHttpChannel = AttributeKey.valueOf("httpChannel-" + reqId);
			channel.attr(keyHttpChannel).set(ctx.channel());
			((FullHttpRequest) msg).retain();
			channel.writeAndFlush(((FullHttpRequest) msg).retain());

		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		closeOnFlush(ctx.channel());
	}
}
