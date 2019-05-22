package com.feng.lin.pass.nat.client.proxy.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.feng.lin.pass.nat.client.bean.Config;
import com.feng.lin.pass.nat.client.bean.Tunel;

public class Router {
	private Router() {
	}

	private static final class Instance {
		private static final Router router = new Router();
	}

	private static final Map<String, List<Map<String, Tunel>>> tunelMap = new ConcurrentHashMap<String, List<Map<String, Tunel>>>();

	public static final Router getInstance() {
		return Instance.router;
	}

	public void parseConfig(Config config) {
		tunelMap.clear();
		config.getTunels().forEach((tunel) -> {

			if (!tunelMap.containsKey(tunel.getProto())) {
				tunelMap.put(tunel.getProto(), new ArrayList<Map<String, Tunel>>());
			}
			Map<String, Tunel> map = new HashMap<>();
			map.put(tunel.getSubDomain() + "." + config.getDomain(), tunel);
			tunelMap.get(tunel.getProto()).add(map);
		});
	}

	public Tunel get(String proto, String domain) {
		List<Map<String, Tunel>> tunelList = tunelMap.get(proto);
		if (tunelList != null) {
			for (Map<String, Tunel> map : tunelList) {
				if (map.containsKey(domain)) {
					return map.get(domain);
				}
			}
		}
		return null;
	}
}
