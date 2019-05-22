package com.feng.lin.pass.nat.client;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feng.lin.pass.nat.client.bean.Config;
import com.feng.lin.pass.nat.client.proxy.route.Router;
import com.feng.lin.pass.nat.client.tunel.TunelClient;
import com.feng.lin.pass.nat.comm.YamReader;
import com.feng.lin.pass.nat.comm.debug.Loger;

public class ClientRunner {
	private static final Logger logger = LoggerFactory.getLogger(ClientRunner.class);
	private static final String CONFIG = "client.yml";

	public static void main(String[] args) {
		Loger.debugLog(logger, () -> "client starting..");
		Optional<Config> configOptional = Optional.ofNullable(
				YamReader.reader(Config.class, ClientRunner.class.getClassLoader().getResourceAsStream(CONFIG)));
		Config config = configOptional.orElseGet(() -> new Config());
		Router.getInstance().parseConfig(config);
		Loger.debugLog(logger, () -> "config:" + config);
		try {
			TunelClient.run(config.getServerAddr(), config.getPort(), config);
		} catch (Exception e) {
			// TODO Auto-generated catch block

			Loger.debugLog(logger, () -> "client start error: " + e.getMessage());
		}
	}

}
