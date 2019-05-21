package com.feng.lin.pass.nat.server;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feng.lin.pass.nat.comm.YamReader;
import com.feng.lin.pass.nat.comm.debug.Loger;
import com.feng.lin.pass.nat.server.bean.Config;
import com.feng.lin.pass.nat.server.http.HttpServer;
import com.feng.lin.pass.nat.server.tunel.TunelServer;

public class ServerRunner {
	private static final Logger logger = LoggerFactory.getLogger(ServerRunner.class);
	private static final String CONFIG = "server.yml";

	public static void main(String[] args) {
		Loger.debugLog(logger, () -> "client starting..");
		Optional<Config> configOptional = Optional.ofNullable(
				YamReader.reader(Config.class, ServerRunner.class.getClassLoader().getResourceAsStream(CONFIG)));
		Config config = configOptional.orElseGet(() -> new Config());
		Loger.debugLog(logger, () -> "config:" + config);
		try {
			TunelServer.run(config.getTunelAddr());
			HttpServer.run(config.getHttpAddr());
		} catch (Exception e) {
			// TODO Auto-generated catch block

			Loger.debugLog(logger, () -> "client start error: " + e.getMessage());
		}

	}

}
