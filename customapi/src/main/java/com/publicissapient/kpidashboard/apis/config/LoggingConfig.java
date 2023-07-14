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

package com.publicissapient.kpidashboard.apis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.publicissapient.kpidashboard.apis.logging.DatabaseLoggingCondition;
import com.publicissapient.kpidashboard.apis.logging.KeyValueLoggingCondition;
import com.publicissapient.kpidashboard.apis.logging.KeyValueLoggingFilter;
import com.publicissapient.kpidashboard.apis.logging.LoggingFilter;

/**
 * Provides Logging config.
 */
@Configuration
public class LoggingConfig {

	/**
	 * Provides key value logging filter.
	 *
	 * @return the key value logging filter
	 */
	@Bean
	@Conditional(KeyValueLoggingCondition.class)
	public KeyValueLoggingFilter splunkConnectionLoggingFilter() {
		return new KeyValueLoggingFilter();
	}

	/**
	 * Provides logging filter.
	 *
	 * @return the logging filter
	 */
	@Bean
	@Conditional(DatabaseLoggingCondition.class)
	public LoggingFilter loggingFilter() {
		return new LoggingFilter();
	}

}
