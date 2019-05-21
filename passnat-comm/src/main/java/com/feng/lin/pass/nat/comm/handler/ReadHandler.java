package com.feng.lin.pass.nat.comm.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;

public class ReadHandler extends ChannelInboundHandlerAdapter {
	private static class HeaderParser implements ByteProcessor {
		private final AppendableCharSequence seq;
		private final int maxLength;
		private int size;

		HeaderParser(AppendableCharSequence seq, int maxLength) {
			this.seq = seq;
			this.maxLength = maxLength;
		}

		public AppendableCharSequence parse(ByteBuf buffer) {
			final int oldSize = size;
			seq.reset();
			int i = buffer.forEachByte(this);
			if (i == -1) {
				size = oldSize;
				return null;
			}
			buffer.readerIndex(i + 1);
			return seq;
		}

		public void reset() {
			size = 0;
		}

		@Override
		public boolean process(byte value) throws Exception {
			char nextByte = (char) (value & 0xFF);
			if (nextByte == HttpConstants.CR) {
				return true;
			}
			if (nextByte == HttpConstants.LF) {
				return false;
			}

			if (++size > maxLength) {
				// TODO: Respond with Bad Request and discard the traffic
				// or close the connection.
				// No need to notify the upstream handlers - just log.
				// If decoding a response, just throw an exception.
				throw newException(maxLength);
			}

			seq.append(nextByte);
			return true;
		}

		protected TooLongFrameException newException(int maxLength) {
			return new TooLongFrameException("HTTP header is larger than " + maxLength + " bytes.");
		}
	}

	private static final class LineParser extends HeaderParser {

		LineParser(AppendableCharSequence seq, int maxLength) {
			super(seq, maxLength);
		}

		@Override
		public AppendableCharSequence parse(ByteBuf buffer) {
			reset();
			return super.parse(buffer);
		}

		@Override
		protected TooLongFrameException newException(int maxLength) {
			return new TooLongFrameException("An HTTP line is larger than " + maxLength + " bytes.");
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
	}

	private static String[] splitInitialLine(AppendableCharSequence sb) {
		int aStart;
		int aEnd;
		int bStart;
		int bEnd;
		int cStart;
		int cEnd;

		aStart = findNonWhitespace(sb, 0);
		aEnd = findWhitespace(sb, aStart);

		bStart = findNonWhitespace(sb, aEnd);
		bEnd = findWhitespace(sb, bStart);

		cStart = findNonWhitespace(sb, bEnd);
		cEnd = findEndOfString(sb);

		return new String[] { sb.subStringUnsafe(aStart, aEnd), sb.subStringUnsafe(bStart, bEnd),
				cStart < cEnd ? sb.subStringUnsafe(cStart, cEnd) : "" };
	}

	private static int findWhitespace(AppendableCharSequence sb, int offset) {
		for (int result = offset; result < sb.length(); ++result) {
			if (Character.isWhitespace(sb.charAtUnsafe(result))) {
				return result;
			}
		}
		return sb.length();
	}

	private static int findNonWhitespace(AppendableCharSequence sb, int offset) {
		for (int result = offset; result < sb.length(); ++result) {
			if (!Character.isWhitespace(sb.charAtUnsafe(result))) {
				return result;
			}
		}
		return sb.length();
	}

	private static int findEndOfString(AppendableCharSequence sb) {
		for (int result = sb.length() - 1; result > 0; --result) {
			if (!Character.isWhitespace(sb.charAtUnsafe(result))) {
				return result + 1;
			}
		}
		return 0;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	
		System.out.println("recevied data:" + msg);
		LineParser lineParser = new LineParser(new AppendableCharSequence(128), 4096);
		ByteBuf buf = (ByteBuf) msg;
		AppendableCharSequence line = lineParser.parse(buf);
		buf.resetReaderIndex();
		if (line == null) {
			return;
		}
		String[] initialLine = splitInitialLine(line);
		if (initialLine.length < 3) {
			return;
		}
		try {
			new DefaultHttpRequest(HttpVersion.valueOf(initialLine[2]), HttpMethod.valueOf(initialLine[0]),
					initialLine[1], true);

			if (ctx.pipeline().get("httpDecoder") != null) {
				ctx.pipeline().replace(ctx.pipeline().get("httpDecoder"), "httpDecoder", new HttpRequestDecoder());
			} else {
				ctx.pipeline().addAfter("readHandler", "httpDecoder", new HttpRequestDecoder());
			}
		} catch (Exception e) {
			try {
				new DefaultHttpResponse(HttpVersion.valueOf(initialLine[0]),
						HttpResponseStatus.valueOf(Integer.parseInt(initialLine[1]), initialLine[2]), true);
				if (ctx.pipeline().get("httpDecoder") != null) {
					ctx.pipeline().replace(ctx.pipeline().get("httpDecoder"), "httpDecoder", new HttpResponseDecoder());
				} else {
					ctx.pipeline().addAfter("readHandler", "httpDecoder", new HttpResponseDecoder());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}

		}
		super.channelRead(ctx, msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		super.channelWritabilityChanged(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}

}
