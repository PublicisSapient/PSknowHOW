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

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.argocd.dto.Application;
import com.publicissapient.kpidashboard.argocd.dto.ApplicationsList;
import com.publicissapient.kpidashboard.argocd.dto.TokenDTO;
import com.publicissapient.kpidashboard.argocd.dto.UserCredentialsDTO;

import lombok.extern.slf4j.Slf4j;

import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.APPLICATIONS_ENDPOINT;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.APPLICATIONS_PARAM;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.AUTHORIZATION_HEADER;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.AUTHTOKEN_ENDPOINT;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.BEARER;

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
	 * 				ArgoCD base url
	 * @param accessToken
	 * 				user access token
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
			log.debug("ArgoCDClient :: getApplications response :: {}", response.getBody());
			return response.getBody();
		} catch (RestClientException ex) {
			log.error("ArgoCDClient :: getApplications Exception occured :: {}", ex.getMessage());
			throw ex;
		}
	}

	/**
	 * Get the Application details by name for the account
	 * 
	 * @param baseUrl
	 * 				ArgoCD base url
	 * @param applicationName
	 * 				name of ArgoCD Application
	 * @param accessToken
	 * 				user access token
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
			log.debug("ArgoCDClient :: getApplicationByName response :: {}", response.getBody());
			return response.getBody();
		} catch (RestClientException ex) {
			log.error("ArgoCDClient :: getApplicationByName Exception occured :: {}", ex.getMessage());
			throw ex;
		}
	}

	/**
	 * Create a new JWT for authentication
	 * 
	 * @param baseUrl
	 * 				ArgoCD base url
	 * @param userCredentialsDTO
	 * 				UserCredentialsDTO
	 * @return String - user access token
	 */
	public String getAuthToken(String baseUrl, UserCredentialsDTO userCredentialsDTO) {
		String url = baseUrl + AUTHTOKEN_ENDPOINT;
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		try {
			ResponseEntity<TokenDTO> response = restTemplate.exchange(URI.create(url), HttpMethod.POST,
					new HttpEntity<>(userCredentialsDTO, requestHeaders), TokenDTO.class);
			return Optional.ofNullable(response.getBody())
		            .map(TokenDTO::getToken)
		            .orElseThrow(() -> new RestClientException("Unable to fetch token for the user"));
		} catch (RestClientException ex) {
			log.error("ArgoCDClient :: getAuthToken Exception occured :: {}", ex.getMessage());
			throw ex;
		}
	}

}
