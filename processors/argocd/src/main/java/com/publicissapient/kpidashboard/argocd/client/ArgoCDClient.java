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

package com.publicissapient.kpidashboard.argocd.client;

import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.APPLICATIONS_ENDPOINT;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.APPLICATIONS_PARAM;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.ARGOCD_CLUSTER_ENDPOINT;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.AUTHORIZATION_HEADER;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.BEARER;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.argocd.dto.Application;
import com.publicissapient.kpidashboard.argocd.dto.ApplicationsList;

import lombok.extern.slf4j.Slf4j;

/**
 * Client for fetching information from ArgoCD
 */
@Service
@Slf4j
public class ArgoCDClient {

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Get the list of Applications associated to the account
	 * 
	 * @param baseUrl
	 *            ArgoCD base url
	 * @param accessToken
	 *            user access token
	 * @return ApplicationList
	 */
	public ApplicationsList getApplications(String baseUrl, String accessToken) {
		String url = baseUrl + APPLICATIONS_ENDPOINT + "?" + APPLICATIONS_PARAM;
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.add(AUTHORIZATION_HEADER, BEARER + accessToken);
		try {
			ResponseEntity<ApplicationsList> response = restTemplate.exchange(URI.create(url), HttpMethod.GET,
					new HttpEntity<>(requestHeaders), ApplicationsList.class);
			return response.getBody();
		} catch (RestClientException ex) {
			log.error("ArgoCDClient :: getApplications Exception occurred :: {}", ex.getMessage());
			throw ex;
		}
	}

	/**
	 * Get the Application details by name for the account
	 * 
	 * @param baseUrl
	 *            ArgoCD base url
	 * @param applicationName
	 *            name of ArgoCD Application
	 * @param accessToken
	 *            user access token
	 * @return Application
	 */
	public Application getApplicationByName(String baseUrl, String applicationName, String accessToken) {
		String url = baseUrl + APPLICATIONS_ENDPOINT + "/" + applicationName;
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.add(AUTHORIZATION_HEADER, BEARER + accessToken);
		try {
			ResponseEntity<Application> response = restTemplate.exchange(URI.create(url), HttpMethod.GET,
					new HttpEntity<>(requestHeaders), Application.class);
			return response.getBody();
		} catch (RestClientException ex) {
			log.error("ArgoCDClient :: getApplicationByName Exception occurred :: {}", ex.getMessage());
			throw ex;
		}
	}

	/**
	 * Get the cluster names associated with the account.
	 *
	 * @param baseUrl
	 *            the ArgoCD base URL
	 * @param accessToken
	 *            the user access token
	 * @return a map where the key is the server URL and the value is the cluster
	 *         name
	 * @throws RestClientException
	 *             if an error occurs while making the REST call
	 * @throws RuntimeException
	 *             if an error occurs while processing the JSON response
	 */
	public Map<String, String> getClusterName(String baseUrl, String accessToken) {
		String url = baseUrl + ARGOCD_CLUSTER_ENDPOINT;
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> serverToNameMap = new HashMap<>();
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.add(AUTHORIZATION_HEADER, BEARER + accessToken);
		try {
			ResponseEntity<String> response = restTemplate.exchange(URI.create(url), HttpMethod.GET,
					new HttpEntity<>(requestHeaders), String.class);
			if (StringUtils.isNotEmpty(response.getBody())) {
				JsonNode root1 = mapper.readTree(response.getBody());
				StreamSupport.stream(root1.path("items").spliterator(), false)
						.forEach(item -> serverToNameMap.put(item.path("server").asText(), item.path("name").asText()));
			}
			return serverToNameMap;
		} catch (RestClientException ex) {
			log.error("ArgoCDClient :: getClusterName Exception occurred :: {}", ex.getMessage());
			throw ex;
		} catch (JsonProcessingException ex) {
			log.error("ArgoCDClient :: getClusterName JsonProcessingException occurred :: {}", ex.getMessage());
			return Collections.emptyMap();
		}
	}
}
