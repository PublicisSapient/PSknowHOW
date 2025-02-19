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

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Class that can be used to bootstrap and launch a GitHubActionApplication
 * application from a Java main method.
 */
@SpringBootApplication
@EnableCaching
@ComponentScan({"com.publicissapient"})
@EnableMongoRepositories(basePackages = {"com.publicissapient.**.repository"})
public class GitHubActionApplication {

	/**
	 * Main thread from where GitHubActionApplication starts.
	 *
	 * @param args
	 *          the command line argument
	 */
	public static void main(final String[] args) {
		SpringApplication.run(GitHubActionApplication.class, args);
	}

	/**
	 * create rest template bean which accept response given by github
	 *
	 * @return RestTemplate RestTemplate
	 * @throws KeyStoreException
	 *           KeyStoreException
	 * @throws NoSuchAlgorithmException
	 *           NoSuchAlgorithmException
	 * @throws KeyManagementException
	 *           KeyManagementException
	 */
	@Bean
	public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		final RestTemplate restTemplate = new RestTemplate();

		SSLContext sslContext = SSLContextBuilder.create()
				.loadTrustMaterial((X509Certificate[] certificateChain, String authType) -> true) // <--- accepts each
				// certificate
				.build();

		Registry<ConnectionSocketFactory> socketRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register(URIScheme.HTTPS.getId(), new SSLConnectionSocketFactory(sslContext))
				.register(URIScheme.HTTP.getId(), new PlainConnectionSocketFactory()).build();

		HttpClient httpClient = HttpClientBuilder.create()
				.setConnectionManager(new PoolingHttpClientConnectionManager(socketRegistry)).setConnectionManagerShared(true)
				.build();

		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		restTemplate.setRequestFactory(requestFactory);
		return restTemplate;
	}
}
