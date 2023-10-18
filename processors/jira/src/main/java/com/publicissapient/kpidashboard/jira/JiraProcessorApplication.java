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

package com.publicissapient.kpidashboard.jira;

import javax.net.ssl.HttpsURLConnection;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * @author pankumar8
 *
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
// @SpringBootApplication - uncomment this line and remove above line when
// spring job repository implemented for mongodb
@EnableCaching
@ComponentScan(basePackages = { "com.publicissapient" })
@EnableMongoRepositories(basePackages = { "com.publicissapient.**.repository" })
@EnableBatchProcessing
@EnableAsync
@EnableScheduling
public class JiraProcessorApplication {

	private static boolean sslHostNameFlag = true;

	public static void main(String[] args) {
		HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> sslHostNameFlag);
		SpringApplication.run(JiraProcessorApplication.class, args);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
