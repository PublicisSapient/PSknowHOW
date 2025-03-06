package com.publicissapient.kpidashboard.apis.hierarchy.integration.helper;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ReaderRetryHelper {

	public static final int MAX_RETRY_ATTEMPT = 3;
	public static final long TIME_INTERVAL_BETWEEN_RETRY = 5000;

	@Retryable
	public <T> T executeWithRetry(RetryableOperation<T> operation) throws Exception {
		RetryTemplate retryTemplate = new RetryTemplate(); // Creating a new RetryTemplate for each retry

		// Configure the retry policy (maximum of 3 retry attempts)
		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
		retryPolicy.setMaxAttempts(MAX_RETRY_ATTEMPT);
		retryTemplate.setRetryPolicy(retryPolicy);

		// Configure the backoff policy (fixed delay of 3000 milliseconds between
		// retries)
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(TIME_INTERVAL_BETWEEN_RETRY);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		return retryTemplate.execute(context -> {
			log.info("Attempt #{}", context.getRetryCount() + 1);
			return operation.execute();
		});
	}

	@FunctionalInterface
	public interface RetryableOperation<T> {
		T execute() throws Exception;
	}
}