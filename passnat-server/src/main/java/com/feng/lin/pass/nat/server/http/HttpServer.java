package com.feng.lin.pass.nat.server.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feng.lin.pass.nat.comm.debug.Loger;
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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpServer {
	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
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
						p.addLast(new HttpRequestDecoder());
						p.addLast(new HttpObjectAggregator(65536));
						p.addLast(new HttpResponseEncoder());
						p.addLast(new HttpServerHandler(false));
					}
				});
	}

	public static void run(int port) {
		new Thread(() -> {
			try {
				Channel ch = b.bind(port).sync().channel();
				Loger.debugLog(logger, () -> "Open your web browser and navigate to " + (false ? "https" : "http")
						+ "://127.0.0.1:" + port + '/');
				ch.closeFuture().sync();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();
			}
		}).start();

	}
}
