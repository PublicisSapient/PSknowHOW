package com.publicissapient.kpidashboard.zephyr.processor.service.impl;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.zephyr.ZephyrTestCaseDTO;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.zephyr.client.ZephyrClient;
import com.publicissapient.kpidashboard.zephyr.config.ZephyrConfig;
import com.publicissapient.kpidashboard.zephyr.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.zephyr.util.ZephyrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class ZephyrServerImpl implements ZephyrClient {

	private static final String TEST_CASE_ENDPOINT = "/testcase/search";
	private static final String INVERTED_COMMA = "\"";
	private static final String PROJECT_KEY = "projectKey = \"";
	private static final String QUERY_PARAM = "&query=";
	private static final String START_AT = "startAt";
	private static final String MAX_RESULTS = "maxResults";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ZephyrConfig zephyrConfig;

	@Autowired
	private ZephyrUtil zephyrUtil;

	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Override
	public List<ZephyrTestCaseDTO> getTestCase(final int startAt, final ProjectConfFieldMapping projectConfig) {
		List<ZephyrTestCaseDTO> testCaseList = new ArrayList<>();
		ProcessorToolConnection toolInfo = projectConfig.getProcessorToolConnection();

		if (StringUtils.isNotEmpty(toolInfo.getUrl()) && StringUtils.isNotEmpty(toolInfo.getApiEndPoint())) {
			String apiEndPoint = toolInfo.getApiEndPoint();
			String zephyrUrl = zephyrUtil.getZephyrUrl(toolInfo.getUrl());

			UriComponentsBuilder builder = zephyrUtil.buildAPIUrl(zephyrUrl, apiEndPoint);
			builder.path(TEST_CASE_ENDPOINT);
			builder.queryParam(MAX_RESULTS, zephyrConfig.getPageSize());
			builder.queryParam(START_AT, startAt);
			StringBuilder queryBuilder = new StringBuilder(builder.build(false).toString());
			queryBuilder.append(QUERY_PARAM).append(PROJECT_KEY);
			queryBuilder.append(projectConfig.getProjectKey());
			queryBuilder.append(INVERTED_COMMA);

			log.info("ZEPHYR query executed {} ....", queryBuilder);
			if (StringUtils.isNotBlank(queryBuilder)) {

				try {
					ResponseEntity<ZephyrTestCaseDTO[]> response = makeRestCall(queryBuilder.toString(),
							ZephyrTestCaseDTO[].class, HttpMethod.GET,
							zephyrUtil.getCredentialsAsBase64String(toolInfo.getUsername(), toolInfo.getPassword()));

					if (response.getStatusCode() == HttpStatus.OK && Objects.nonNull(response.getBody()) ) {
						testCaseList = Arrays.asList(response.getBody());
					} else {
						String statusCode = response.getStatusCode().toString();
						log.error("Error while fetching projects from {}. with status {}", queryBuilder, statusCode);
						throw new RestClientException(
								"Got different status code: " + statusCode + " : " + response.getBody());
					}

				} catch (Exception exception) {
					log.error("Error while fetching projects from {}", exception.getMessage());
					throw new RestClientException("Error while fetching projects from {}", exception);
				}

			}
		}
		return testCaseList;
	}

	/**
	 * REST client to make a REST call to the Zephyr cloud
	 *
	 * @param url
	 * @param type
	 * @param httpMethod
	 * @return {@link ResponseEntity}
	 */
	private <T extends Object> ResponseEntity<T> makeRestCall(final String url, final Class<T> type,
			HttpMethod httpMethod, final String credentials) {
		return restTemplate.exchange(url, httpMethod, zephyrUtil.buildAuthenticationHeader(credentials), type);
	}
}
