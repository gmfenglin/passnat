package com.feng.lin.pass.nat.server.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feng.lin.pass.nat.comm.debug.Loger;
import com.feng.lin.pass.nat.server.tunel.TunelServer;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.AttributeKey;

@Sharable
public class TunelServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static final Logger logger = LoggerFactory.getLogger(TunelServerHandler.class);
	public static final Map<String, Channel> channelMap = new ConcurrentHashMap<>();
	public static final Map<String, String> domainMap = new ConcurrentHashMap<>();

	public TunelServerHandler(boolean autoRelease) {
		super(autoRelease);
		// TODO Auto-generated constructor stub
	}

	static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Channel channel = ctx.channel();
		closeOnFlush(channel);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		Loger.debugLog(logger, () -> "TunelServerHandler channelRead0:" + msg);
		Channel channel = ctx.channel();
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest request = (FullHttpRequest) msg;
			if ("/regist".equals(request.uri().toString())) {
				String content = request.content().toString(Charset.defaultCharset());
				System.out.println(content);
				JSONObject jObject = JSON.parseObject(content);
				String domain = jObject.getString("domain");
				channel.attr(AttributeKey.valueOf("domain")).set(domain);

				JSONArray subDomainArray = jObject.getJSONArray("tunels");
				for (int i = 0; i < subDomainArray.size(); i++) {
					JSONObject subDomainObject = subDomainArray.getJSONObject(i);
					String host = subDomainObject.getString("subDomain") + "." + domain;
					domainMap.put(host, domain);
				}

				channelMap.put(domain, channel);
				FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
						Unpooled.wrappedBuffer("linked sucess".getBytes()));
				response.headers().set(CONTENT_TYPE, "text/plain");
				response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

				response.headers().set(CONNECTION, KEEP_ALIVE);
				channel.writeAndFlush(response);
			}

		} else if (msg instanceof FullHttpResponse) {
			AttributeKey<Channel> keyHttpChannel = AttributeKey.valueOf("httpChannel");
			Channel httpChannel = ctx.channel().attr(keyHttpChannel).get();
			if (httpChannel != null && httpChannel.isActive()) {
				httpChannel.writeAndFlush(msg);
			}
		}

	}

}
