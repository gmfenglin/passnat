package com.feng.lin.pass.nat.comm.debug;

import java.util.function.Supplier;

import org.slf4j.Logger;

public class Loger {
	public final static void debugLog(Logger logger, Supplier<String> supplier) {
		if (logger.isDebugEnabled()) {
			logger.debug(supplier.get());
		}
	}
}
