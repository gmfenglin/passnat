package com.feng.lin.pass.nat.server.https;

import com.feng.lin.pass.nat.server.handler.HttpServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;

public class HttpsServer {
	private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
	private static final ServerBootstrap b = new ServerBootstrap();
	static {
		b.option(ChannelOption.SO_BACKLOG, 1024);
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast(new SslHandler(new HttpSslContextFactory().createSSLEngine()));
						p.addLast(new HttpRequestDecoder());
						p.addLast(new HttpObjectAggregator(65536));
						p.addLast(new HttpResponseEncoder());
						p.addLast(new HttpServerHandler(true));
					}
				});
	}

	public static void run(int port) {
		new Thread(() -> {
			try {
				Channel ch = b.bind(port).sync().channel();
				System.err.println("Open your web browser and navigate to " + (false ? "https" : "http")
						+ "://127.0.0.1:" + port + '/');
				ch.closeFuture().sync();

			} catch (Exception e) {
			} finally {
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();
			}
		}).start();

	}
}
