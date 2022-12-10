package com.publicissapient.kpidashboard.common.config;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	// private static final LogHolder LOGDATA = new LogHolder();

	private static final Logger LOG = LoggerFactory.getLogger(AsyncExceptionHandler.class);

	static final String ASYNC_EXCEPTION = "asyncException";

	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		/*
		 * LOGDATA.put(LogKeys.LogEvent.ACTION, ASYNC_EXCEPTION);
		 * LOGDATA.put(LoggableAsyncError.ASYNC_KEY, loggableAsyncError);
		 * LOG.error(appendEntries(LOGDATA.getAttributes()), ASYNC_EXCEPTION);
		 * LOG.error("DetailedError", ex);
		 */
	}

}