package com.feng.lin.pass.nat.comm.message;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class PassNatMessageDecoder extends ReplayingDecoder<Void> {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int id = in.readInt();
		if (id == 0xabef0101) {
			PassNatMessage message = new PassNatMessage();
			message.setId(id);
			int length = in.readInt();
			message.setLength(length);
			byte protocol = in.readByte();
			message.setProtocol(protocol);
			byte type = in.readByte();
			message.setType(type);
			byte[] body = new byte[length];
			in.readBytes(body);
			message.setBody(body);
			out.add(message);
		}
	}

}
