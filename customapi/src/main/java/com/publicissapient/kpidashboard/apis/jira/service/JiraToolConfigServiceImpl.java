package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.jira.model.BoardDetailsDTO;
import com.publicissapient.kpidashboard.apis.jira.model.BoardRequestDTO;
import com.publicissapient.kpidashboard.apis.jira.model.JiraBoardListResponse;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.dto.AssigneeResponseDTO;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;

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
	private static final String RESOURCE_JIRA_ASSINGEE_ENDPOINT = "user/assignable/search?project=";
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

	public List<BoardDetailsDTO> getJiraBoardDetailsList(BoardRequestDTO boardRequestDTO) {

		List<BoardDetailsDTO> responseList = new ArrayList<>();
		Optional<Connection> optConnection = connectionRepository
				.findById(new ObjectId(boardRequestDTO.getConnectionId()));
		if (optConnection.isPresent()) {
			Connection connection = optConnection.get();
			String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
			HttpEntity<?> httpEntity = getHttpEntity(connection);
			fetchBoardDetailsRestAPICall(boardRequestDTO, responseList, baseUrl, httpEntity);
			return responseList;
		}
		return responseList;
	}

	private void fetchBoardDetailsRestAPICall(BoardRequestDTO boardRequestDTO, List<BoardDetailsDTO> responseList,
			String baseUrl, HttpEntity<?> httpEntity) {
		long startAt = 0;
		long nextPageIndex = startAt;
		boolean isLast = false;
		do {
			try {
				String url = String.format(baseUrl + RESOURCE_JIRA_BOARD_ENDPOINT, boardRequestDTO.getProjectKey(),
						nextPageIndex, boardRequestDTO.getBoardType());

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

		if (connection.isVault()) {
			ToolCredential credential = toolCredentialProvider
					.findCredential(connection.getUsername() == null ? null : connection.getUsername().trim());
			if (credential != null) {
				username = credential.getUsername();
				password = credential.getPassword();
			}
		} else {
			username = connection.getUsername() == null ? null : connection.getUsername().trim();
			password = connection.getPassword() == null ? null : restAPIUtils.decryptPassword(connection.getPassword());
		}
		HttpHeaders headers = restAPIUtils.getHeaders(username, password);
		return new HttpEntity<>(headers);

	}

	public AssigneeResponseDTO getProjectAssigneeDetails(String projectConfigId) {
		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();
		List<AssigneeDetails> assigneeDetailsResponseList = new ArrayList<>();
		Optional<ProjectBasicConfig> basicConfig = projectBasicConfigRepository.findById(new ObjectId(projectConfigId));
		if (basicConfig.isPresent()) {
			ProjectBasicConfig projectBasicConfig = basicConfig.get();
			List<ProjectToolConfig> projectToolConfigs = projectToolConfigRepository
					.findByToolNameAndBasicProjectConfigId(Constant.TOOL_JIRA, new ObjectId(projectConfigId));
			projectToolConfigs.stream().forEach(projectToolConfig -> {
				Optional<Connection> optConnection = connectionRepository.findById(projectToolConfig.getConnectionId());
				if (optConnection.isPresent()) {
					Connection connection = optConnection.get();
					String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
					String endPoint = connection.getApiEndPoint() == null ? null : connection.getApiEndPoint().trim();
					String url = createApiUrl(baseUrl, endPoint);
					getApiCreationAndCall(assigneeDetailsResponseList, projectToolConfig, connection, url);
				}
			});

			assigneeResponseDTO.setBasicProjectConfigId(new ObjectId(projectConfigId));
			assigneeResponseDTO.setAssigneeDetailsList(assigneeDetailsResponseList);
			assigneeResponseDTO.setProjectName(projectBasicConfig.getProjectName());
		}
		return assigneeResponseDTO;
	}

	private void getApiCreationAndCall(List<AssigneeDetails> assigneeDetailsResponseList,
			ProjectToolConfig projectToolConfig, Connection connection, String url) {
		if (StringUtils.isNotEmpty(url)) {
			url = url + RESOURCE_JIRA_ASSINGEE_ENDPOINT + projectToolConfig.getProjectKey().trim();
			HttpEntity<?> httpEntity = getHttpEntity(connection);
			List<AssigneeDetails> assigneeDetailsResponse = fetchAssigneeDetailsRestAPICall(projectToolConfig,
					httpEntity, url);
			assigneeDetailsResponseList.addAll(assigneeDetailsResponse);

		}
	}

	public List<AssigneeDetails> fetchAssigneeDetailsRestAPICall(ProjectToolConfig toolConfig, HttpEntity<?> httpEntity,
			String url) {

		List<AssigneeDetails> assigneeRoles = new ArrayList<>();

		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				log.info(response.getBody());
				assigneeRoles = convertListFromArray(response, assigneeRoles);
			} else {
				String statusCode = response.getStatusCode().toString();
				log.error("Error while fetching BoardList from {}. with status {}", url, statusCode);
			}

		} catch (Exception exception) {
			log.error("Error while fetching assignees for projectKey Id {}:  {}", toolConfig.getProjectKey(),
					exception.getMessage());
		}

		return assigneeRoles;
	}

	public List<AssigneeDetails> convertListFromArray(ResponseEntity<String> response, List<AssigneeDetails> list) {
		JSONParser jsonParser = new JSONParser();
		JSONArray jsonArray = null;
		try {
			jsonArray = (JSONArray) jsonParser.parse(response.getBody());

			for (Object obj : jsonArray) {
				AssigneeDetails assigneeRole = new AssigneeDetails();
				JSONObject jsonObject = (JSONObject) obj;
				if (jsonObject != null) {
					assigneeRole.setName(jsonObject.get("name").toString());
					assigneeRole.setDisplayName(jsonObject.get("displayName").toString());
					list.add(assigneeRole);
				}
			}
		} catch (ParseException e) {
			log.error("error while fetching assignee details {}", e.getMessage());
		}
		return list;
	}

	private String createApiUrl(String baseUrl, String endPoint) {

		if (StringUtils.isNotEmpty(baseUrl) && StringUtils.isNotEmpty(endPoint)) {
			return baseUrl.endsWith("/") ? baseUrl.concat(endPoint) : baseUrl.concat("/").concat(endPoint);
		}
		return null;
	}
}
