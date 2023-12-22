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

package com.publicissapient.kpidashboard.githubaction;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Class that can be used to bootstrap and launch a GitHubActionApplication
 * application from a Java main method.
 */
@SpringBootApplication
@EnableCaching
@ComponentScan({ "com.publicissapient" })
@EnableMongoRepositories(basePackages = { "com.publicissapient.**.repository" })
public class GitHubActionApplication {

	/**
	 * Main thread from where GitHubActionApplication starts.
	 * 
	 * @param args
	 *            the command line argument
	 */
	public static void main(final String[] args) {
		SpringApplication.run(GitHubActionApplication.class, args);
	}

	/**
	 * create rest template bean which accept response given by github
	 * 
	 * @return RestTemplate RestTemplate
	 * @throws KeyStoreException
	 *             KeyStoreException
	 * @throws NoSuchAlgorithmException
	 *             NoSuchAlgorithmException
	 * @throws KeyManagementException
	 *             KeyManagementException
	 */
	@Bean
	public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		org.apache.hc.client5.http.classic.HttpClient httpClient = (org.apache.hc.client5.http.classic.HttpClient)HttpClients.custom().setSSLSocketFactory(csf).build();

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		return new RestTemplate(requestFactory);
	}

}
