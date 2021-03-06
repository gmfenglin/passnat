package com.feng.lin.pass.nat.comm.coder.http;

import java.util.ArrayList;
import java.util.List;

import com.feng.lin.pass.nat.comm.message.PassNatMessage;
import com.feng.lin.pass.nat.comm.message.PassNatMessageType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.util.ReferenceCountUtil;

public class RequestEncoder extends HttpRequestEncoder {

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
		 List<Object>  outTmp= new ArrayList<>();
		super.encode(ctx, msg, outTmp);
		outTmp.forEach((buf)->{
			ByteBuf bbuf=(ByteBuf) buf;
			PassNatMessage message=new PassNatMessage();
			byte [] bufArray=new byte[bbuf.readableBytes()];
			bbuf.readBytes(bufArray);
			message.setBody(bufArray);
			message.setLength(bufArray.length);
			message.setProtocol(PassNatMessageType.protocol_http);
			message.setType(PassNatMessageType.protocol_http_request);
			out.add(message);
		});
	}

}
