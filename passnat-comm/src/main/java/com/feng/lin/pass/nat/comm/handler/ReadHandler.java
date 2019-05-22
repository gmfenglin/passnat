package com.feng.lin.pass.nat.comm.handler;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class ReadHandler extends HttpObjectDecoder {
	public static int MAX_LENGTH = 4096 * 1000;

	public ReadHandler() {
		this(MAX_LENGTH, 8192, 8192);
	}

	/**
	 * Creates a new instance with the specified parameters.
	 */
	public ReadHandler(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
		super(maxInitialLineLength, maxHeaderSize, maxChunkSize, true);
	}

	public ReadHandler(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders) {
		super(maxInitialLineLength, maxHeaderSize, maxChunkSize, true, validateHeaders);
	}

	public ReadHandler(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders,
			int initialBufferSize) {
		super(maxInitialLineLength, maxHeaderSize, maxChunkSize, true, validateHeaders, initialBufferSize);
	}

	private boolean decodingRequest;

	@Override
	protected boolean isDecodingRequest() {
		// TODO Auto-generated method stub
		return decodingRequest;
	}

	@Override
	protected HttpMessage createMessage(String[] initialLine) throws Exception {
		HttpMessage httpMessage = null;
		try {
			httpMessage = new DefaultHttpRequest(HttpVersion.valueOf(initialLine[2]),
					HttpMethod.valueOf(initialLine[0]), initialLine[1], validateHeaders);
			decodingRequest = true;
		} catch (Exception e) {
			try {
				httpMessage = new DefaultHttpResponse(HttpVersion.valueOf(initialLine[0]),
						HttpResponseStatus.valueOf(Integer.parseInt(initialLine[1]), initialLine[2]), validateHeaders);
				decodingRequest = false;
			} catch (Exception ex) {
				throw new Exception("bad-message");
			}
		} finally {
			return httpMessage;
		}
	}

	@Override
	protected HttpMessage createInvalidMessage() {
		return new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/bad-request", validateHeaders);
	}

}