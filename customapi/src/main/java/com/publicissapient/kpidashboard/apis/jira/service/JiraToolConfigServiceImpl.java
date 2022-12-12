package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import org.bson.types.ObjectId;
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
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;

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

		if (connection.isVault()){
			ToolCredential credential = toolCredentialProvider.findCredential(connection.getUsername() == null ? null : connection.getUsername().trim());
			if (credential != null){
				username = credential.getUsername();
				password = credential.getPassword();
			}
		} else {
			username = connection.getUsername() == null ? null : connection.getUsername().trim();
			password = connection.getPassword() == null ? null
					: restAPIUtils.decryptPassword(connection.getPassword());
		}

		HttpHeaders headers = restAPIUtils.getHeaders(username, password);
		return new HttpEntity<>(headers);
	}
}
