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

package com.publicissapient.kpidashboard.jiratest;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.client.RestTemplate;

/**
 * @author HirenKumar Babariya JiraTestProcessorApplication configuration and
 *         bootstrap
 */
@SpringBootApplication
@EnableCaching
@ComponentScan({ "com.publicissapient" })
@EnableMongoRepositories(basePackages = "com.publicissapient.**.repository")
public class JiraTestProcessorApplication {

	/**
	 * Main thread of operation that runs the Spring Boot processor application.
	 * 
	 * @param args
	 *            Any command line arguments that need to be captured at runtime
	 *            (currently, none are used)
	 */
	public static void main(final String[] args) {
		SpringApplication.run(JiraTestProcessorApplication.class, args);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
