package com.feng.lin.pass.nat.comm.message;

import com.feng.lin.pass.nat.comm.coder.http.RequestEncoder;
import com.feng.lin.pass.nat.comm.coder.http.ResponseEncoder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class PassNatWriterHandler extends ChannelOutboundHandlerAdapter {
	public static final String PASSNAT_HANDLER_WRITE = "passNatHandlerWrite";

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg == null) {
			return;
		}
		HandlerInChannel handlerInChannel = new HandlerInChannel(ctx.channel());
		if (msg instanceof PassNatMessage) {
			super.write(ctx, msg, promise);
		} else if (msg instanceof FullHttpResponse) {
			ctx.pipeline().addBefore(PASSNAT_HANDLER_WRITE, "Encoder",
					handlerInChannel.get("ResponseEncoder", () -> new ResponseEncoder()));
			super.write(ctx, msg, promise);
		} else if (msg instanceof FullHttpRequest) {
			ctx.pipeline().addBefore(PASSNAT_HANDLER_WRITE, "Encoder",
					handlerInChannel.get("RequestEncoder", () -> new RequestEncoder()));
			super.write(ctx, msg, promise);
		} else {
			super.write(ctx, msg, promise);
		}

	}

}
