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

package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.jira.model.BoardDetailsDTO;
import com.publicissapient.kpidashboard.apis.jira.model.BoardRequestDTO;
import com.publicissapient.kpidashboard.apis.jira.model.JiraBoardListResponse;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.AssigneeDetailsDTO;
import com.publicissapient.kpidashboard.common.model.application.dto.AssigneeResponseDTO;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * class for jira tool config fetch data api
 *
 * @author Hirenkumar babariya
 *
 */
@Service
@Slf4j
public class JiraToolConfigServiceImpl {

	private static final String RESOURCE_JIRA_BOARD_ENDPOINT = "/rest/agile/1.0/board?projectKeyOrId=%s&startAt=%d&type=%s";
	final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RestAPIUtils restAPIUtils;

	@Autowired
	private ConnectionRepository connectionRepository;

	@Autowired
	private ToolCredentialProvider toolCredentialProvider;

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private ProjectBasicConfigRepository projectBasicConfigRepository;

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	public List<BoardDetailsDTO> getJiraBoardDetailsList(BoardRequestDTO boardRequestDTO) {

		List<BoardDetailsDTO> responseList = new ArrayList<>();
		Optional<Connection> optConnection = connectionRepository
				.findById(new ObjectId(boardRequestDTO.getConnectionId()));
		try {
			if (optConnection.isPresent()) {
				Connection connection = optConnection.get();
				String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
				HttpEntity<?> httpEntity = getHttpEntity(connection);
				fetchBoardDetailsRestAPICall(boardRequestDTO, responseList, baseUrl, httpEntity);
				return responseList;
			}
		} catch (RestClientException exception) {
			log.error("exception occured while trying to hit api.");
		}
		return responseList;
	}

	public void fetchBoardDetailsRestAPICall(BoardRequestDTO boardRequestDTO, List<BoardDetailsDTO> responseList,
			String baseUrl, HttpEntity<?> httpEntity) {
		long startAt = 0;
		long nextPageIndex = startAt;
		boolean isLast = false;
		do {
			try {
				String url = String.format(new StringBuilder(baseUrl).append(RESOURCE_JIRA_BOARD_ENDPOINT).toString(),
						boardRequestDTO.getProjectKey(), nextPageIndex, boardRequestDTO.getBoardType());

				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					JiraBoardListResponse jiraBoardListResponse = mapper.readValue(response.getBody(),
							JiraBoardListResponse.class);
					if (!jiraBoardListResponse.getIsLast().equalsIgnoreCase("true")) {
						nextPageIndex = jiraBoardListResponse.getStartAt() + jiraBoardListResponse.getMaxResults();
						isLast = true;
					} else {
						isLast = false;
					}
					setBoardListResponse(responseList, jiraBoardListResponse);
				} else {
					String statusCode = response.getStatusCode().toString();
					log.error("Error while fetching BoardList from {}. with status {}", url, statusCode);
				}

			} catch (Exception exception) {
				log.error("Error while fetching boardList for projectKey Id {}:  {}", boardRequestDTO.getProjectKey(),
						exception.getMessage());
			}
		} while (isLast);
	}

	private void setBoardListResponse(List<BoardDetailsDTO> responseList, JiraBoardListResponse jiraBoardListResponse) {
		jiraBoardListResponse.getValues().stream().forEach(jiraBoardValueResponse -> {
			BoardDetailsDTO boardDetailsDTO = new BoardDetailsDTO();
			boardDetailsDTO.setBoardId(jiraBoardValueResponse.getId());
			boardDetailsDTO.setBoardName(jiraBoardValueResponse.getName());
			responseList.add(boardDetailsDTO);
		});
	}

	private HttpEntity<?> getHttpEntity(Connection connection) {
		String username = "";
		String password = "";
		HttpHeaders headers = new HttpHeaders();
		if (connection.isJaasKrbAuth()) {
			KerberosClient client = new KerberosClient(connection.getJaasConfigFilePath(),
					connection.getKrb5ConfigFilePath(), connection.getJaasUser(), connection.getSamlEndPoint(),
					connection.getBaseUrl());
			client.login(customApiConfig.getSamlTokenStartString(), customApiConfig.getSamlTokenEndString(),
					customApiConfig.getSamlUrlStartString(), customApiConfig.getSamlUrlEndString());
			password = client.getCookies();
			headers = restAPIUtils.addHeaders(headers, "Cookie", password);
		} else if (connection.isVault()) {
			ToolCredential credential = toolCredentialProvider
					.findCredential(connection.getUsername() == null ? null : connection.getUsername().trim());
			if (credential != null) {
				username = credential.getUsername();
				password = credential.getPassword();
			}
			headers = restAPIUtils.getHeaders(username, password);
		} else if (connection.isBearerToken()) {
			String patOAuthToken = restAPIUtils.decryptPassword(connection.getPatOAuthToken());
			headers = restAPIUtils.getHeadersForPAT(patOAuthToken);
		} else {
			username = connection.getUsername() == null ? null : connection.getUsername().trim();
			password = connection.getPassword() == null ? null : restAPIUtils.decryptPassword(connection.getPassword());
			headers = restAPIUtils.getHeaders(username, password);
		}
		return new HttpEntity<>(headers);

	}

	public AssigneeResponseDTO getProjectAssigneeDetails(String projectConfigId) {
		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();
		AssigneeDetails assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigId(projectConfigId);
		List<AssigneeDetailsDTO> assigneeDetailsDTOResponseList = new ArrayList<>();
		if (assigneeDetails != null && CollectionUtils.isNotEmpty(assigneeDetails.getAssignee())) {
			assigneeDetails.getAssignee().stream().forEach(assignee -> {
				AssigneeDetailsDTO assigneeDetailsDTO = new AssigneeDetailsDTO();
				// assigneeId will be unique id for assignee
				assigneeDetailsDTO.setName(assignee.getAssigneeId());
				assigneeDetailsDTO.setDisplayName(assignee.getAssigneeName());
				assigneeDetailsDTOResponseList.add(assigneeDetailsDTO);
			});
			Collections.sort(assigneeDetailsDTOResponseList, (AssigneeDetailsDTO o1, AssigneeDetailsDTO o2) -> o1
					.getDisplayName().compareTo(o2.getDisplayName()));
		}
		assigneeResponseDTO.setBasicProjectConfigId(new ObjectId(projectConfigId));
		assigneeResponseDTO.setAssigneeDetailsList(assigneeDetailsDTOResponseList);
		return assigneeResponseDTO;
	}
}
