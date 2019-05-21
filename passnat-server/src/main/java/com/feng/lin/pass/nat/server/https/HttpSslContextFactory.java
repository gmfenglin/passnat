package com.feng.lin.pass.nat.server.https;

import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class HttpSslContextFactory {
	private static final String PROTOCOL = "SSLv3";// �ͻ��˿���ָ��ΪSSLv3����TLSv1.2
	private static SSLContext sslContext = null;
	static {
		String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
		if (algorithm == null) {
			algorithm = "SunX509";
		}
		SSLContext serverContext = null;
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(HttpSslContextFactory.class.getClassLoader().getResourceAsStream("keystore.jks"),
					"1q2w3e4r".toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			kmf.init(ks, "1q2w3e4r".toCharArray());
			serverContext = SSLContext.getInstance(PROTOCOL);
			serverContext.init(kmf.getKeyManagers(), null, null);
		} catch (Exception e) {
			throw new Error("Failed to initialize the server SSLContext", e);
		}
		sslContext = serverContext;
	}

	public static SSLEngine createSSLEngine() {
		SSLEngine sslEngine = sslContext.createSSLEngine();
		sslEngine.setUseClientMode(false);
		sslEngine.setNeedClientAuth(false);
		return sslEngine;
	}
}
