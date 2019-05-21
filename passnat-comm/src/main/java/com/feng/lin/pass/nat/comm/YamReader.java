package com.feng.lin.pass.nat.comm;

import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

public class YamReader {
	public final static <T> T reader(Class<T> type, InputStream input) {
		Yaml yaml = new Yaml();
		return yaml.loadAs(input, type);
	}
}
