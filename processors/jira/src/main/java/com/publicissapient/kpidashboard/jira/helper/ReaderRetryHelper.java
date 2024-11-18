/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.jira.helper;

import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

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
		return retryTemplate.execute(context -> operation.execute());
	}

	@FunctionalInterface
	public interface RetryableOperation<T> {
		T execute() throws Exception;
	}
}
