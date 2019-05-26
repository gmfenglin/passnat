package com.feng.lin.pass.nat.comm;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

public class RequestParser {
	private FullHttpRequest fullReq;

	/**
	 * ����һ��������
	 * 
	 * @param req
	 */
	public RequestParser(FullHttpRequest req) {
		this.fullReq = req;
	}

	/**
	 * �����������
	 * 
	 * @return ����������������ļ�ֵ��, ���û�в���, �򷵻ؿ�Map
	 *
	 * @throws BaseCheckedException
	 * @throws IOException
	 */
	public Map<String, String> parse() throws Exception {
		HttpMethod method = fullReq.method();

		Map<String, String> parmMap = new HashMap<>();

		if (HttpMethod.GET == method) {
			// ��GET����
			QueryStringDecoder decoder = new QueryStringDecoder(fullReq.uri());
			decoder.parameters().entrySet().forEach(entry -> {
				// entry.getValue()��һ��List, ֻȡ��һ��Ԫ��
				parmMap.put(entry.getKey(), entry.getValue().get(0));
			});
		} else if (HttpMethod.POST == method) {
			// ��POST����
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullReq);
			decoder.offer(fullReq);

			List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

			for (InterfaceHttpData parm : parmList) {

				Attribute data = (Attribute) parm;
				parmMap.put(data.getName(), data.getValue());
			}

		} else {
			// ��֧����������
		}

		return parmMap;
	}
}
