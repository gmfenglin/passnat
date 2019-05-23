package com.feng.lin.pass.nat.client.proxy.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

public class Client {
	private static final EventLoopGroup group = new NioEventLoopGroup();
	private static Bootstrap bootstrap = new Bootstrap();
	static {
		try {
			bootstrap.group(group).option(ChannelOption.SO_KEEPALIVE, false).channel(NioSocketChannel.class);
			bootstrap.handler(new ChannelInitializer<Channel>() {
				protected void initChannel(Channel channel) throws Exception {
					// channel.pipeline().addLast(sslCtx.newHandler(channel.alloc()));
					// °üº¬±àÂëÆ÷ºÍ½âÂëÆ÷
					channel.pipeline().addLast(new HttpRequestEncoder());

					// ¾ÛºÏ

					channel.pipeline().addLast(new HttpResponseDecoder());
					channel.pipeline().addLast(new HttpObjectAggregator(1024 * 10 * 1024));
					// ½âÑ¹
					channel.pipeline().addLast(new HttpContentDecompressor());

				}
			});
		} catch (Exception e) {
		}
	}

	public static void start(String host, int port, FullHttpRequest request, Channel tunelChannel)
			throws InterruptedException {
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
						future.channel().writeAndFlush(request);
					}
				});

				channelFuture.channel().closeFuture().sync();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();

	}

}
