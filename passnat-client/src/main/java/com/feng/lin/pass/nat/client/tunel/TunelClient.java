package com.feng.lin.pass.nat.client.tunel;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.feng.lin.pass.nat.client.bean.Config;
import com.feng.lin.pass.nat.comm.debug.Loger;
import com.feng.lin.pass.nat.comm.message.PassNatMessageDecoder;
import com.feng.lin.pass.nat.comm.message.PassNatMessageEncoder;
import com.feng.lin.pass.nat.comm.message.PassNatReaderHandler;
import com.feng.lin.pass.nat.comm.message.PassNatWriterHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;

public class TunelClient {
	private static final Logger logger = LoggerFactory.getLogger(TunelClient.class);
	private static final EventLoopGroup group = new NioEventLoopGroup();
	private static Config config;
	private static String host;
	private static int port;
	private static Bootstrap b = new Bootstrap();
	static {
		try {
			SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
					.build();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(sslCtx.newHandler(ch.alloc()));
							p.addLast(new LoggingHandler(LogLevel.INFO));
							p.addLast(new IdleStateHandler(5, 5, 10));
							p.addLast(new PassNatMessageDecoder());
							p.addLast(new PassNatMessageEncoder());
							p.addLast(PassNatReaderHandler.PASSNAT_HANDLER_READ, new PassNatReaderHandler(false));
							p.addLast(PassNatWriterHandler.PASSNAT_HANDLER_WRITE, new PassNatWriterHandler());
							p.addLast(new TunelClientHandler());
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static ChannelFuture connect() throws Exception {
		ChannelFuture f = b.connect(host, port).sync();

		f.addListener((ChannelFuture future) -> {
			if (future.isSuccess()) {

				Loger.debugLog(logger, () -> "host: " + host + ",port:" + port);
				Channel channel = future.channel();

				URI url = new URI("/regist");
				FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET,
						url.toASCIIString(), Unpooled.wrappedBuffer(JSON.toJSONString(config).getBytes("UTF-8")));
				request.headers().set(HttpHeaderNames.HOST, host);
				request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
				channel.writeAndFlush(request);
				Loger.debugLog(logger, () -> "regist channel:" + channel);
			} else {
				TimeUnit.SECONDS.sleep(20);
				future.channel().eventLoop().schedule(() -> {
					try {
						connect();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}, 10, TimeUnit.SECONDS);
			}
		});
		return f;

	}

	public static void reconnect() {
		run(host, port, config);
	}

	public static void run(String inHost, int inPort, Config inconfig) {
		config = inconfig;
		host = inHost;
		port = inPort;
		Runnable runnable = () -> {

			try {

				connect().channel().closeFuture().sync();

			} catch (Exception e) {
				System.out.println("run:reconnect after 20 seconds.");
				try {
					TimeUnit.SECONDS.sleep(20);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				run(host, port, config);

			}
		};
		new Thread(runnable).start();
	}
}
