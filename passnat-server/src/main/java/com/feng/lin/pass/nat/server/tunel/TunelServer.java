package com.feng.lin.pass.nat.server.tunel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feng.lin.pass.nat.comm.debug.Loger;
import com.feng.lin.pass.nat.comm.message.PassNatMessageDecoder;
import com.feng.lin.pass.nat.comm.message.PassNatMessageEncoder;
import com.feng.lin.pass.nat.comm.message.PassNatReaderHandler;
import com.feng.lin.pass.nat.comm.message.PassNatWriterHandler;
import com.feng.lin.pass.nat.server.handler.TunelServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.timeout.IdleStateHandler;

public class TunelServer {
	private static final Logger logger = LoggerFactory.getLogger(TunelServer.class);
	private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
	private static final ServerBootstrap b = new ServerBootstrap();
	static {
		try {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(sslCtx.newHandler(ch.alloc()));
							//p.addLast(new IdleStateHandler(10, 10, 20));
							p.addLast(new PassNatMessageDecoder());
							p.addLast(new PassNatMessageEncoder());
							p.addLast(PassNatReaderHandler.PASSNAT_HANDLER_READ, new PassNatReaderHandler(true));
							p.addLast(PassNatWriterHandler.PASSNAT_HANDLER_WRITE, new PassNatWriterHandler());
							p.addLast(new TunelServerHandler(false));
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void run(int port) {
		new Thread(() -> {
			try {

				ChannelFuture f = b.bind(port).sync();
				Loger.debugLog(logger, () -> "listening port:" + port);
				f.channel().closeFuture().sync();
			} catch (Exception e) {

			} finally {
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();
			}
		}).start();

	}
}
