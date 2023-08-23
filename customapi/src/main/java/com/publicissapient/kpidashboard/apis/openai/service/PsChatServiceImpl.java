
package com.publicissapient.kpidashboard.apis.openai.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.application.OpenAiTraceLog;
import com.publicissapient.kpidashboard.common.repository.application.OpenAiTraceLogRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.openai.dto.ChatDataResponseDTO;
import com.publicissapient.kpidashboard.apis.openai.dto.ChatMessageDTO;
import com.publicissapient.kpidashboard.apis.openai.dto.OptionsDTO;
import com.publicissapient.kpidashboard.apis.openai.dto.PromptDetailsDTO;
import com.publicissapient.kpidashboard.apis.openai.dto.enums.AssistantType;
import com.publicissapient.kpidashboard.apis.openai.dto.enums.GPTModel;
import com.publicissapient.kpidashboard.apis.openai.model.ChatDTO;
import com.publicissapient.kpidashboard.apis.openai.model.IssueDetail;
import com.publicissapient.kpidashboard.apis.openai.model.PromptRequest;
import com.publicissapient.kpidashboard.apis.openai.model.PsChatSprintDetails;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PsChatServiceImpl implements PsChatService {

	@Autowired
	RestTemplate restTemplate;
	@Autowired
	CustomApiConfig customApiConfig;
	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	SprintRepository sprintRepository;
	@Autowired
	JiraIssueRepository jiraIssueRepository;
	@Autowired
	OpenAiTraceLogRepository openAiTraceLogRepository;
	@Autowired
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	private String url = "https://api.psnext.info";

	// private static final String BEARER_TOKEN =
	// "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJVc2VySW5mbyI6eyJpZCI6MTI4NjgsInJvbGVzIjpbImRlZmF1bHQiXSwicGF0aWQiOiIzNTA1MWZmOC1iZjJlLTQyNGUtYTM2Ni1iZjhlNzY4OTA5N2MifSwiaWF0IjoxNjkxNTYxNjA0LCJleHAiOjE3MDE5Mjk2MDR9.yDx_wt_XPiRxx1I-960PiGFhIFsl1Du4JxRDrChJRr4";
	@Override
	public ChatDTO getChat(String chatId) throws HttpClientErrorException {
		HttpHeaders headers = new HttpHeaders();
		String bearerToken = customApiConfig.getPsChatToken();
		headers.set("Authorization", "Bearer " + bearerToken);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		ResponseEntity<ChatDTO> response = restTemplate.exchange(url + "/chats/" + chatId, HttpMethod.GET, entity,
				ChatDTO.class);

		return response.getBody();
	}

	public ChatDTO sendPrompt(PromptRequest promptRequest) throws HttpClientErrorException {
		HttpHeaders headers = new HttpHeaders();
		// headers.set("Authorization", "Bearer " + BEARER_TOKEN);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<PromptRequest> entity = new HttpEntity<>(promptRequest, headers);

		ResponseEntity<ChatDTO> response = restTemplate.exchange(url + "/api/chat/", HttpMethod.POST, entity,
				ChatDTO.class);

		return response.getBody();
	}

	public String getRecommendationForPrompt(GPTModel gptModel, AssistantType assistantType, String prompt)
			throws InvalidRequestException {

		OptionsDTO optionsDTO = OptionsDTO.builder().model(gptModel.getValue()).assistant(assistantType.getRole())
				.build();

		PromptDetailsDTO promptDetailsDTO = PromptDetailsDTO.builder().async(false).message(prompt).options(optionsDTO)
				.build();

		HttpHeaders headers = new HttpHeaders();
		String bearerToken = customApiConfig.getPsChatToken();
		headers.set("Authorization", "Bearer " + bearerToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<PromptDetailsDTO> entity = new HttpEntity<>(promptDetailsDTO, headers);

		ResponseEntity<ChatDataResponseDTO> response = restTemplate.exchange(url + "/api/chat", HttpMethod.POST, entity,
				ChatDataResponseDTO.class);

		List<ChatMessageDTO> chatMessages = Objects.requireNonNull(response.getBody()).getData().getMessages();
		return chatMessages.get(chatMessages.size() - 1).getContent();

	}

	public ServiceResponse getIterationPrompt(GPTModel gptModel, AssistantType assistantType, String sprintId)
			throws InvalidRequestException {
		SprintDetails sprintDetail = sprintRepository.findBySprintID(sprintId);
		OpenAiTraceLog existingSprintChat = openAiTraceLogRepository.findBySprintId(sprintId);
		ServiceResponse response = new ServiceResponse(false, "Server Gateway Error", null);
		if(!sprintDetail.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE) && existingSprintChat != null){
			response.setMessage("Recommendation fetched successfully form db");
			response.setSuccess(true);
			response.setData(existingSprintChat.getRecommendations());
			return response;
		}
		final PsChatSprintDetails psChatSprintDetails = getPsChatSprintDetails(sprintDetail);

		OptionsDTO optionsDTO = OptionsDTO.builder().model(gptModel.getValue()).assistant(assistantType.getRole())
				.build();

/*
		PromptDetailsDTO promptDetails = PromptDetailsDTO.builder().async(true).message(psChatSprintDetails.toString())
				.options(optionsDTO).build();
*/

		List<String> prompt = new ArrayList<>();
		prompt.add(
				"How many issues on average have spilled i.e belong to notcompletedIssue Breakup by issue types use issueDetails for details regarding issue");
		prompt.add(
				"How much scope have been added and removed in terms of issueKey & story points use issueDetails for details regarding issue");
		prompt.add(
				"Out of the incomplete issues ,Which status had max tickets , Which assignee had maximum tickets use issueDetails for details regarding issue");
		prompt.add(
				"Story with max number of bugs use linkedStoryId to determine it use issueDetails for details regarding issue");
		prompt.add(
				"for all the completed issue what is date they have been completed then find What has been my average daily completion in Story points use issueDetails for details regarding issue");
		prompt.add(
				"On which day was the first story completed, give number and its corresponding date use issueDetails for details regarding issue");
		prompt.add(
				"What percentage of issues get completed in the initial 50% of activatedDate and final 50% of completedDate use issueDetails for details regarding issue");


		HttpHeaders headers = new HttpHeaders();
		String bearerToken = customApiConfig.getPsChatToken();
		headers.set("Authorization", "Bearer " + bearerToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Split the psChatSprintDetails string into chunks of 7000 tokens
		StringBuilder psChatSprintDetailsStr = new StringBuilder();
		psChatSprintDetailsStr.append("First i will be sending sprint details in few prompt store them first & latter prompt will be asked around it ");
		psChatSprintDetailsStr.append(psChatSprintDetails.toString().replaceAll("\\s", ""));
		List<String> psChatSprintDetailsChunks = splitStringByTokenCount(psChatSprintDetailsStr.toString(), 1900);

		List<String> responses = new ArrayList<>();

		  // Send all the chunks first

		String chatId = "";
		for (String chunk : psChatSprintDetailsChunks) {
			PromptDetailsDTO chunkPromptDetailsDTO = PromptDetailsDTO.builder().id(chatId).async(false).message(chunk)
				  .options(optionsDTO).build();

		  HttpEntity<PromptDetailsDTO> chunkEntity = new HttpEntity<>(chunkPromptDetailsDTO, headers);

		  try {
			  ResponseEntity<ChatDataResponseDTO> contextResponse = restTemplate.exchange(url + "/api/chat", HttpMethod.POST, chunkEntity, ChatDataResponseDTO.class);
			  chatId = Objects.requireNonNull(contextResponse.getBody()).getData().getId();
			  log.debug(contextResponse.toString());
		  } catch (Exception e) {
			  log.error("Error in fetching the data from psChat" + e);
		  }
	  }

	  // Send prompts one by one
		for (String promptQuestion : prompt) {

			PromptDetailsDTO promptDetailsDTO = PromptDetailsDTO.builder().id(chatId).async(false).message(promptQuestion)
					.options(optionsDTO).build();

			HttpEntity<PromptDetailsDTO> entity = new HttpEntity<>(promptDetailsDTO,
					headers);

			try {
				ResponseEntity<ChatDataResponseDTO> chatResponse = restTemplate.exchange(url + "/api/chat", HttpMethod.POST,
						entity, ChatDataResponseDTO.class);
				log.debug("Fetching ps-chat response for api [{}] ",promptQuestion);
				if (chatResponse.getStatusCode().is2xxSuccessful()) {
					response.setMessage("Recommendation fetched successfully from ps-chat");
					response.setSuccess(true);
					List<ChatMessageDTO> chatMessages = Objects.requireNonNull(chatResponse.getBody()).getData()
							.getMessages();
					responses.add(chatMessages.get(chatMessages.size() - 1).getContent());
					response.setData(responses);
				} else if (chatResponse.getStatusCode().is5xxServerError()) {
					response.setMessage("Server Error");
				}
			} catch (Exception e) {
				log.error("Error in fetching the data from psChat" + e);
			}
		}
		if(CollectionUtils.isNotEmpty(responses)){
			OpenAiTraceLog openAiTraceLog = existingSprintChat == null ? new OpenAiTraceLog() : existingSprintChat;
			openAiTraceLog.setSprintId(sprintId);
			openAiTraceLog.setRecommendations(responses);
			openAiTraceLog.setLastSyncDateTime(System.currentTimeMillis());
			openAiTraceLogRepository.save(openAiTraceLog);
			log.debug("Saving ps-chat response for sprint {} ",sprintId);
		}
		return response;
	}

	private PsChatSprintDetails getPsChatSprintDetails(SprintDetails sprintDetail) {
		PsChatSprintDetails psChatSprintDetails = new PsChatSprintDetails();
		psChatSprintDetails.setSprintID(sprintDetail.getSprintID());
		psChatSprintDetails.setSprintName(sprintDetail.getSprintName());
		psChatSprintDetails.setActivatedDate(sprintDetail.getActivatedDate());
		psChatSprintDetails.setCompleteDate(sprintDetail.getCompleteDate());
		psChatSprintDetails.setEndDate(sprintDetail.getEndDate());
		psChatSprintDetails.setStartDate(sprintDetail.getStartDate());
		psChatSprintDetails.setState(sprintDetail.getState());
		if (sprintDetail.getCompletedIssues() != null) {
			psChatSprintDetails.setCompletedIssues(
					sprintDetail.getCompletedIssues().stream().map(SprintIssue::getNumber).collect(Collectors.toSet()));
		}
		if (sprintDetail.getNotCompletedIssues() != null) {
			psChatSprintDetails.setNotCompletedIssues(sprintDetail.getNotCompletedIssues().stream()
					.map(SprintIssue::getNumber).collect(Collectors.toSet()));
		}
		if (sprintDetail.getPuntedIssues() != null) {
			psChatSprintDetails.setRemovedIssues(
					sprintDetail.getPuntedIssues().stream().map(SprintIssue::getNumber).collect(Collectors.toSet()));
		}
		if (sprintDetail.getAddedIssues() != null) {
			psChatSprintDetails.setAddedIssues(sprintDetail.getAddedIssues());
		}
		Set<IssueDetail> issueDetails = new HashSet<>();
		List<String> issue = sprintDetail.getTotalIssues().stream().map(SprintIssue::getNumber)
				.collect(Collectors.toList());
		String basicProjectConfigId = sprintDetail.getBasicProjectConfigId().toString();
		List<JiraIssue> jiraIssues = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(issue,
				basicProjectConfigId);
		List<JiraIssueCustomHistory> jiraIssueHistories = jiraIssueCustomHistoryRepository
				.findByStoryIDInAndBasicProjectConfigIdIn(issue, Collections.singletonList(basicProjectConfigId));
		FieldMapping fieldMapping = configHelperService.getFieldMapping(sprintDetail.getBasicProjectConfigId());
		Map<String, Object> startCloseDateMap = KpiDataHelper.startCompletedDateFromIssueHistory(jiraIssueHistories,
				sprintDetail, fieldMapping);
		jiraIssues.forEach(s -> {
			IssueDetail issueDetail = new IssueDetail();
			Map<String, LocalDate> startCloseDate = (Map<String, LocalDate>) startCloseDateMap
					.getOrDefault(s.getNumber(), null);
			issueDetail.setIssueKey(s.getNumber());
			issueDetail.setPriority(s.getPriority());
			issueDetail.setStatus(s.getStatus());
			issueDetail.setTypeName(s.getTypeName());
			issueDetail.setStoryPoint(s.getStoryPoints());
			issueDetail.setOriginalEstimateInMin(s.getOriginalEstimateMinutes());
			issueDetail.setRemainingEstimateInMin(s.getRemainingEstimateMinutes());
			issueDetail.setAssignee(s.getAssigneeName());
			issueDetail.setDueDate(s.getDueDate());
			issueDetail.setLinkedStoryIds(s.getDefectStoryID());
			if (startCloseDate != null) {
				issueDetail.setIssueStartDate(startCloseDate.getOrDefault(CommonConstant.ISSUE_START_DATE, null));
				issueDetail.setIssueCompleteDate(startCloseDate.getOrDefault(CommonConstant.ISSUE_CLOSED_DATE, null));
			}
			issueDetail.setDueDate(s.getDueDate());
			if (s.getReleaseVersions() != null && !s.getReleaseVersions().isEmpty()) {
				issueDetail.setReleaseVersion(s.getReleaseVersions().get(s.getReleaseVersions().size() - 1));
			}
			issueDetails.add(issueDetail);
		});
		psChatSprintDetails.setIssueDetails(issueDetails);
		return psChatSprintDetails;
	}

	private List<String> splitStringByTokenCount(String input, int tokenCount) {
		List<String> chunks = new ArrayList<>();

		int start = 0;
		while (start < input.length()) {
			int end = Math.min(start + tokenCount, input.length());
			chunks.add(input.substring(start, end));
			start = end;
		}

		return chunks;
	}
}
