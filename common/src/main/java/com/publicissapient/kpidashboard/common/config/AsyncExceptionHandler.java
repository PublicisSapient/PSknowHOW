package com.publicissapient.kpidashboard.common.config;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(AsyncExceptionHandler.class);

	static final String ASYNC_EXCEPTION = "asyncException";

	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		LOG.error(ASYNC_EXCEPTION, ex);
	}

}