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

package com.publicissapient.kpidashboard.bitbucket.util;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.apache.hc.client5.http.classic.HttpClient;

import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;

import java.time.Duration;

/**
 * The Class BitbucketRestOperations.
 */
@Component
public class BitbucketRestOperations implements RestOperationsFactory<RestOperations> {

	/**
	 * Gets the instance of RestOperations
	 *
	 * @return the rest operations
	 */
	@Override
	public RestOperations getTypeInstance() {
		//TODO:setReadTimeOut is depricated and removed from spring
		HttpClient client = (HttpClient) HttpClientBuilder.create().disableAutomaticRetries().build();
		return new RestTemplateBuilder().requestFactory(() -> new HttpComponentsClientHttpRequestFactory(client)).setConnectTimeout(Duration.ofSeconds(20_000)).
				setReadTimeout(Duration.ofSeconds(20_000)).build();
	}
}
