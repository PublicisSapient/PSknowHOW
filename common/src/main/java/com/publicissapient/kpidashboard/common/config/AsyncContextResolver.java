package com.publicissapient.kpidashboard.common.config;

import java.util.Map;
import java.util.Objects;

import com.publicissapient.kpidashboard.common.context.ExecutionLogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class AsyncContextResolver implements TaskDecorator {

	private static final Logger LOG = LoggerFactory.getLogger(AsyncContextResolver.class);

	public AsyncContextResolver() {
		LOG.info("AsyncContextResolver (a TaskDecorator for async) initiated.");
	}

	@Override
	public Runnable decorate(Runnable runnable) {
		Map<String, String> contextMap = MDC.getCopyOfContextMap();
		ExecutionLogContext context = ExecutionLogContext.getContext();
		return () -> {
			try {
				if (Objects.nonNull(contextMap)) {
					MDC.setContextMap(contextMap);
				}

				if (Objects.nonNull(context) && Objects.nonNull(context.getRequestId())) {
					ExecutionLogContext.updateContext(context);
				}
				runnable.run();
			} finally {
				ExecutionLogContext.getContext().destroy();
				MDC.clear();
			}
		};
	}

}
