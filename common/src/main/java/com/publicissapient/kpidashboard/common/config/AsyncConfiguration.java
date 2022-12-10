package com.publicissapient.kpidashboard.common.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

	@Value("${async-pool-size:4}")
	private int poolSize;

	@Value("${async-max-pool-size:10}")
	private int maxPoolSize;

	private static final int MAX_QUEUE_CAPACITY = 100;

	@Override
	public Executor getAsyncExecutor() {
		return initAsyncExecutor();
	}

	@Bean(destroyMethod = "shutdown")
	public Executor initAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(poolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(MAX_QUEUE_CAPACITY);
		executor.setTaskDecorator(new AsyncContextResolver());
		// executor.setThreadNamePrefix(Constants.ASYNC_THREAD_PREFIX);
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new AsyncExceptionHandler();
	}
}
