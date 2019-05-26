package com.feng.lin.pass.nat.server.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feng.lin.pass.nat.comm.debug.Loger;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

@Sharable
public class TunelServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static final Logger logger = LoggerFactory.getLogger(TunelServerHandler.class);
	public static final Map<String, Map<String, Channel>> channelMap = new ConcurrentHashMap<>();
	public static final Map<String, List<String>> subDomainMap = new ConcurrentHashMap<>();
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
		System.out.println("exceptionCaught:" + cause.getMessage());
		ctx.close().addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				clear(future.channel());
			}
		});
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel() + "is inactive");
		clear(ctx.channel());

	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		Channel channel = ctx.channel();
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest request = (FullHttpRequest) msg;
			if ("/regist".equals(request.uri().toString())) {
				String content = request.content().toString(Charset.defaultCharset());
				System.out.println(content);
				JSONObject jObject = JSON.parseObject(content);
				String domain = jObject.getString("domain");
				String domainClientId = jObject.getString("clientId");
				FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
						Unpooled.wrappedBuffer("linked sucess".getBytes()));
				if (!channelMap.containsKey(domain)) {
					channelMap.put(domain, new ConcurrentHashMap<>());
				}
				if (!channelMap.get(domain).containsKey(domainClientId)) {
					channelMap.get(domain).put(domainClientId, channel);
					channel.attr(AttributeKey.valueOf("domain")).set(domain);
					channel.attr(AttributeKey.valueOf("clientId")).set(domainClientId);
					JSONArray subDomainArray = jObject.getJSONArray("tunels");
					for (int i = 0; i < subDomainArray.size(); i++) {
						JSONObject subDomainObject = subDomainArray.getJSONObject(i);
						String host = subDomainObject.getString("subDomain") + "." + domain;
						if (!subDomainMap.containsKey(host)) {
							subDomainMap.put(host, new CopyOnWriteArrayList<>());
						}
						if (!subDomainMap.get(host).contains(domainClientId)) {
							subDomainMap.get(host).add(domainClientId);
						}
						domainMap.put(host, domain);
					}

					response.headers().set(CONNECTION, KEEP_ALIVE);
					response.headers().set(CONTENT_TYPE, "text/plain");
					response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

					channel.writeAndFlush(response);
				} else {
					response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_ACCEPTABLE,
							Unpooled.wrappedBuffer("clientId is used by other".getBytes()));
					response.headers().set(CONNECTION, HttpHeaderValues.CLOSE);
					response.headers().set(CONTENT_TYPE, "text/plain");
					response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

					channel.writeAndFlush(response).addListener(new ChannelFutureListener() {

						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							channel.close();
						}
					});
				}

			}

		} else if (msg instanceof FullHttpResponse) {
			FullHttpResponse response = (FullHttpResponse) msg;
			System.out.println("response:"+response);
			AttributeKey<Channel> keyHttpChannel = AttributeKey
					.valueOf("httpChannel-" + response.headers().get("reqId"));
			Channel httpChannel = ctx.channel().attr(keyHttpChannel).get();
			if (httpChannel != null && httpChannel.isActive()) {
				httpChannel.writeAndFlush(msg);
			}
		}

	}

	private void clear(Channel channel) {
		AttributeKey<String> attributeDomainKey = AttributeKey.valueOf("domain");
		AttributeKey<String> attributeClientKey = AttributeKey.valueOf("clientId");
		String domain = channel.attr(attributeDomainKey).get();
		String clientId = channel.attr(attributeClientKey).get();
		if (domain != null && clientId != null && channelMap.get(domain) != null) {
			if (channelMap.get(domain).containsKey(clientId)) {
				channelMap.get(domain).remove(clientId);
			}

			if (channelMap.get(domain).isEmpty()) {
				domainMap.entrySet().forEach((entry) -> {
					if (subDomainMap.get(entry.getKey()).contains(clientId)) {
						subDomainMap.get(entry.getKey()).remove(clientId);
					}
					if (subDomainMap.get(entry.getKey()).isEmpty()) {
						subDomainMap.remove(entry.getKey());
					}
				});
				channelMap.entrySet().forEach((entry) -> {
					domainMap.entrySet().forEach((entryDomain) -> {
						if (entry.getKey().equals(entryDomain.getValue())) {
							domainMap.remove(entryDomain.getKey());
						}
					});

				});

				channelMap.remove(domain);
			} else {
				domainMap.entrySet().forEach((entry) -> {
					if (subDomainMap.get(entry.getKey()).contains(clientId)) {
						subDomainMap.get(entry.getKey()).remove(clientId);
					}
					if (subDomainMap.get(entry.getKey()).isEmpty()) {
						domainMap.remove(entry.getKey());
						subDomainMap.remove(entry.getKey());
					}
				});
			}
		}
		Loger.debugLog(logger, () -> {
			return (JSON.toJSONString(channelMap) + JSON.toJSONString(subDomainMap) + JSON.toJSONString(domainMap));
		});
	}

}
