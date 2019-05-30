package com.feng.lin.pass.nat.comm.coder.http;

import com.feng.lin.pass.nat.comm.message.HandlerDestory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObjectAggregator;

public class PassNatHttpObjectAggregator extends HttpObjectAggregator implements HandlerDestory {
	private ChannelHandlerContext ctx;

	public PassNatHttpObjectAggregator(int maxContentLength) {
		super(maxContentLength);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		this.ctx=ctx;
		
	}

	@Override
	public void destory() {
		if(ctx!=null) {
			try {
				super.handlerRemoved(ctx);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	}

}
