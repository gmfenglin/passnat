package com.feng.lin.pass.nat.client.proxy.http;

import java.net.URI;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;

public class Clients {
	private static final EventLoopGroup group = new NioEventLoopGroup();
	private static Bootstrap bootstrap = new Bootstrap();
	static {
		try {
			SSLContext clientContext = SSLContext.getInstance("TLSv1");
			clientContext.init(null, SecureChatTrustManagerFactory.getTrustManagers(), null);
			SSLEngine engine = clientContext.createSSLEngine();
			engine.setUseClientMode(true);
			bootstrap.group(group).option(ChannelOption.SO_KEEPALIVE, false).channel(NioSocketChannel.class);
			bootstrap.handler(new ChannelInitializer<Channel>() {
				protected void initChannel(Channel channel) throws Exception {
					channel.pipeline().addLast("ssl", new SslHandler(engine));
					channel.pipeline().addLast(new HttpContentDecompressor());
					channel.pipeline().addLast(new HttpResponseDecoder());
					channel.pipeline().addLast(new HttpObjectAggregator(1024 * 10 * 1024));
					channel.pipeline().addLast(new HttpRequestEncoder());

				}
			});
		} catch (Exception e) {
		}
	}

	public static void start(String host, int port, FullHttpRequest request, Channel tunelChannel)
			throws InterruptedException {
		System.out.println("https://" + host + ":" + port);
		new Thread(() -> {
			try {

				ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
				channelFuture.addListener((ChannelFuture future) -> {
					if (future.isSuccess()) {
						if (future.channel().pipeline().context(ClientHandler.class) != null) {
							future.channel().pipeline().remove(ClientHandler.class);
						}
						future.channel().pipeline()
								.addLast(new ClientHandler(request.headers().get("reqId"), tunelChannel));

						if (request.headers().get("Referer") != null) {
							String newReferer = request.headers().get("Referer").replace(request.headers().get("Host"),
									host);
							request.headers().remove("Referer");
							request.headers().add("Referer", newReferer);
						}
						request.headers().remove("Host");
						request.headers().add("Host", host);
						System.out.println(request);
						future.channel().writeAndFlush(request);
					}
				});

				channelFuture.channel().closeFuture().sync();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();

	}

	public static void main(String[] args) {
		try {

			URI uriGet = new URI("/");
			FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
					uriGet.toASCIIString());
			request.headers().set("Connection", "keep-alive");
			request.headers().set("Host", "www.baidu.com");
			request.headers().set("reqId", "dc4ef799-8016-4d2f-86b0-eaeef048b1ac");
			request.headers().set("protocol", "https");
			start("www.baidu.com", 443, request, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
