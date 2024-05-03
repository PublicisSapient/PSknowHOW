package com.publicissapient.kpidashboard.apis.jenkins.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JenkinsToolConfigServiceImpl {

	private static final String RESOURCE_JOBS_ENDPOINT = "/api/json?tree=jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url]]]]]]]]]]]]";
	private static final String JOBS = "jobs";
	private static final String JOB_URL = "url";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RestAPIUtils restAPIUtils;

	@Autowired
	private ConnectionRepository connectionRepository;

	/**
	 *
	 * @param connectionId
	 *            the jenkins server connection details
	 * @return @{@code List<String>} job name list for build/deploy job type
	 */
	public List<String> getJenkinsJobNameList(String connectionId) {

		List<String> responseList = new ArrayList<>();
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		if (optConnection.isPresent()) {
			Connection connection = optConnection.get();
			String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
			String username = connection.getUsername() == null ? null : connection.getUsername().trim();
			String password = connection.getApiKey() == null ? null
					: restAPIUtils.decryptPassword(connection.getApiKey());

			String url = baseUrl + RESOURCE_JOBS_ENDPOINT;

			HttpEntity<?> httpEntity = new HttpEntity<>(restAPIUtils.getHeaders(username, password));
			try {

				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					JSONArray jsonArray = restAPIUtils.convertJSONArrayFromResponse(response.getBody(), JOBS);
					List<String> jobNameKeyList = restAPIUtils.convertListFromMultipleArray(jsonArray, JOB_URL);
					if (CollectionUtils.isNotEmpty(jobNameKeyList)) {
						responseList.addAll(jobNameKeyList);
					}
				} else {
					String statusCode = response.getStatusCode().toString();
					log.error("Error while fetching getJenkinsJobNameList from {}. with status {}", url, statusCode);
				}

			} catch (Exception exception) {
				log.error("Error while fetching getJenkinsJobNameList from {}:  {}", url, exception.getMessage());
			}
			return responseList;
		}
		return responseList;
	}
}
