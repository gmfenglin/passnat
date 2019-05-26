package com.feng.lin.pass.nat.comm.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PassNatMessageEncoder extends MessageToByteEncoder<PassNatMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, PassNatMessage msg, ByteBuf out) throws Exception {
		out.writeInt(msg.getId());
		out.writeInt(msg.getLength());
		out.writeByte(msg.getProtocol());
		out.writeByte(msg.getType());
		out.writeBytes(msg.getBody());
	}

}
