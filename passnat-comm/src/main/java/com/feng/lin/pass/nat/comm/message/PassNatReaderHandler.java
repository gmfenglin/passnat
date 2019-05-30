package com.feng.lin.pass.nat.comm.message;

import com.feng.lin.pass.nat.comm.coder.http.PassNatHttpObjectAggregator;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

public class PassNatReaderHandler extends ChannelInboundHandlerAdapter {
	public static final String PASSNAT_HANDLER_READ = "passNatHandlerRead";
	public static int MAX_LENGTH = 4096 * 1000;
	private boolean isServer;

	public PassNatReaderHandler(boolean isServer) {
		this.isServer = isServer;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		HandlerInChannel handlerInChannel = new HandlerInChannel(ctx.channel());
		handlerInChannel.clear();
		super.channelInactive(ctx);

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		HandlerInChannel handlerInChannel = new HandlerInChannel(ctx.channel());
		handlerInChannel.clear();
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (isServer && event.state() == IdleState.ALL_IDLE) {

				PassNatMessage message = new PassNatMessage();
				String content = "offline";
				message.setLength(content.getBytes().length);
				message.setProtocol(PassNatMessageType.protocol_base);
				message.setType(PassNatMessageType.protocol_base_offline);
				message.setBody(content.getBytes());
				// �����ʱ�Ự

				ChannelFuture writeAndFlush = ctx.writeAndFlush(message);
				writeAndFlush.addListener(new ChannelFutureListener() {

					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						HandlerInChannel handlerInChannel = new HandlerInChannel(ctx.channel());
						handlerInChannel.clear();
						future.channel().close();
					}
				});

			} else if (!isServer && event.state() == IdleState.ALL_IDLE) {
				PassNatMessage message = new PassNatMessage();
				String content = "heart beat send to server";
				message.setLength(content.getBytes().length);
				message.setProtocol(PassNatMessageType.protocol_base);
				message.setType(PassNatMessageType.protocol_base_heartbeat);
				message.setBody(content.getBytes());
				ctx.writeAndFlush(message);
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg == null) {
			return;
		}
		HandlerInChannel handlerInChannel = new HandlerInChannel(ctx.channel());
		if (msg instanceof PassNatMessage) {
			PassNatMessage messageIn = (PassNatMessage) msg;
			if (messageIn.getProtocol() == PassNatMessageType.protocol_base) { // ����Э��
				if (isServer && messageIn.getType() == PassNatMessageType.protocol_base_heartbeat) {
					PassNatMessage message = new PassNatMessage();
					String content = "heart beat send to client";
					message.setLength(content.getBytes().length);
					message.setProtocol(PassNatMessageType.protocol_base);
					message.setType(PassNatMessageType.protocol_base_heartbeat);
					message.setBody(content.getBytes());
					ctx.writeAndFlush(message);
					ReferenceCountUtil.release(msg);
				} else if (messageIn.getType() == PassNatMessageType.protocol_base_offline) {
					System.err.println(new String(messageIn.getBody(), "utf-8"));
					ctx.close();
					ReferenceCountUtil.release(msg);
				}

			} else if (messageIn.getProtocol() == PassNatMessageType.protocol_http) {// httpЭ��

				if (messageIn.getType() == PassNatMessageType.protocol_http_request) {// ����
					ctx.pipeline().addAfter(PASSNAT_HANDLER_READ, "Decoder",
							handlerInChannel.get("HttpRequestDecoder", () -> new HttpRequestDecoder()));
				} else if (messageIn.getType() == PassNatMessageType.protocol_http_response) {// ��Ӧ

					ctx.pipeline().addAfter(PASSNAT_HANDLER_READ, "Decoder",
							handlerInChannel.get("HttpResponseDecoder", () -> new HttpResponseDecoder()));
				}

				ctx.pipeline().addAfter("Decoder", "HttpObjectAggregator", handlerInChannel.get("HttpObjectAggregator",
						() -> new PassNatHttpObjectAggregator(MAX_LENGTH)));

				super.channelRead(ctx, Unpooled.wrappedBuffer(messageIn.getBody()));
			}
		} else {
			super.channelRead(ctx, msg);
		}

	}

}
