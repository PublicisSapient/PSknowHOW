package com.publicissapient.kpidashboard.apis.auth.service;

/**
 * aksshriv1
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthNAuthServiceImpl implements AuthNAuthService {

	private static final String CHECK_POLICY_END_POINT = "PSKnowHow/policies/check";
	private static final String FETCH_POLICY_END_POINT = "PSKnowHow/policies";
	private static final String HTTP_ENTITY = "httpEntity {}";
	private static final String RESPONSE = "response {}";
	private static final String DATA_FOUND = "data found";
	private static final String FETCHED_RESPONSE = "fetched response {}";
	private static final String ERROR_CODE = "Error while fetching from {}. with status {}";
	private static final String ERROR_MESSAGE = "Error while fetching from {}:  {}";

	@Autowired
	AuthProperties authProperties;
	@Autowired
	private RestTemplate restTemplate;

	@Override
	public ServiceResponse checkPoliciesByResource(String apiKey) {
		log.info("checking Action Policy Rules for KnowHow");
		String actionPolicyUrl = getCheckActionPolicyUrl(CHECK_POLICY_END_POINT);
		HttpEntity<?> httpEntity = new HttpEntity<>(CommonUtils.getHeaders(apiKey, true));
		log.info(HTTP_ENTITY, httpEntity);
		ParameterizedTypeReference<ServiceResponse> typeReference = new ParameterizedTypeReference<ServiceResponse>() {
		};
		return getAuthNAuthResponse(restTemplate.exchange(actionPolicyUrl, HttpMethod.POST, httpEntity, typeReference),
				actionPolicyUrl);
	}

	@Override
	public ServiceResponse fetchActionPolicyByResource(String apiKey) {
		log.info("fetching Action Policy Rules from central auth");
		String actionPolicyUrl = getCheckActionPolicyUrl(FETCH_POLICY_END_POINT);
		HttpEntity<?> httpEntity = new HttpEntity<>(CommonUtils.getHeaders(apiKey, true));
		log.info(HTTP_ENTITY, httpEntity);
		ParameterizedTypeReference<ServiceResponse> typeReference = new ParameterizedTypeReference<ServiceResponse>() {
		};
		return getAuthNAuthResponse(restTemplate.exchange(actionPolicyUrl, HttpMethod.GET, httpEntity, typeReference),
				actionPolicyUrl);
	}

	/**
	 * This method returns Action Policy url
	 * 
	 * @return Action Policy URL
	 */
	private String getCheckActionPolicyUrl(String checkPolicyEndPoint) {

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(authProperties.getCentralAuthBaseURL());
		uriBuilder.path("/api/");
		uriBuilder.path(checkPolicyEndPoint);
		return uriBuilder.toUriString();
	}

	/**
	 * 
	 * @param responseEntity
	 * @param url
	 * @return
	 */
	public ServiceResponse getAuthNAuthResponse(ResponseEntity<ServiceResponse> responseEntity, String url) {
		ServiceResponse fetchDataResponse = new ServiceResponse();
		try {
			log.info(RESPONSE, responseEntity);
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				log.info(DATA_FOUND);
				fetchDataResponse = responseEntity.getBody();
				log.info(FETCHED_RESPONSE, fetchDataResponse);
			} else {
				String statusCode = responseEntity.getStatusCode().toString();
				log.error(ERROR_CODE, url, statusCode);
			}
		} catch (Exception exception) {
			log.error(ERROR_MESSAGE, url, exception.getMessage());
		}
		return fetchDataResponse;
	}
}
