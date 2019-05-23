package com.feng.lin.pass.nat.client.tunel;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feng.lin.pass.nat.client.bean.Tunel;
import com.feng.lin.pass.nat.client.proxy.http.Client;
import com.feng.lin.pass.nat.client.proxy.http.Clients;
import com.feng.lin.pass.nat.client.proxy.route.Router;
import com.feng.lin.pass.nat.comm.debug.Loger;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

public class TunelClientHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static final Logger logger = LoggerFactory.getLogger(TunelClientHandler.class);

	public TunelClientHandler() {
		super(false);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("client exceptionCaught:" + cause.getLocalizedMessage());
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("client Inactive");
		TunelClient.reconnect();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.ALL_IDLE) {
				URI url = new URI("/heartbeat");
				FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET,
						url.toASCIIString());
				request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				ctx.channel().writeAndFlush(request);
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		Loger.debugLog(logger, () -> "channelRead0 TunelClientHandler: " + msg);
		Channel channel = ctx.channel();
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest request = (FullHttpRequest) msg;
			System.out.println("content:" + request.content().toString(Charset.defaultCharset()));
			Optional<Tunel> tunelOptional = Optional.ofNullable(
					Router.getInstance().get(request.headers().get("protocol"), request.headers().get("host")));
			if (tunelOptional.isPresent()) {
				Tunel tunel = tunelOptional.get();
				Loger.debugLog(logger, () -> "call proxy: " + request.uri());
				AttributeKey<String> keyHost = AttributeKey.valueOf("host");
				AttributeKey<String> connectHost = AttributeKey.valueOf("connectHost");
				ctx.channel().attr(connectHost)
						.set(tunel.getSchema() + "://" + tunel.getHost() +((tunel.getPort()==443 || tunel.getPort()==80)?"": ":" + tunel.getPort()));
				ctx.channel().attr(keyHost)
						.set(request.headers().get("protocol") + "://" + request.headers().get("Host"));
				if (tunel.getSchema().equals("http")) {

					Client.start(tunel.getHost(), tunel.getPort(), (FullHttpRequest) msg, ctx.channel());
				} else if (tunel.getSchema().equals("https")) {
					Clients.start(tunel.getHost(), tunel.getPort(), (FullHttpRequest) msg, ctx.channel());
				}

			}

		} else if (msg instanceof FullHttpResponse) {

		}
	}

}
