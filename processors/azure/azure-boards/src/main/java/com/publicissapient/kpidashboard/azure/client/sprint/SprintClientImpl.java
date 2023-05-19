package com.publicissapient.kpidashboard.azure.client.sprint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.repository.AzureProcessorRepository;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Hiren Babariya
 *
 */
@Service
@Slf4j
public class SprintClientImpl implements SprintClient {

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private AzureProcessorRepository azureProcessorRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	/**
	 * 
	 * @param projectConfig
	 * @param sprintDetailsSet
	 * @param azureAdapter
	 * @param azureServer
	 */
	@Override
	public void prepareSprintReport(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet,
			AzureAdapter azureAdapter, AzureServer azureServer) {

		FieldMapping fieldMapping = projectConfig.getFieldMapping();
		List<String> completedIssuesStatus = fieldMapping.getJiraIterationCompletionStatusCustomField();
		ObjectId azureProcessorId = azureProcessorRepository.findByProcessorName(ProcessorConstants.AZURE).getId();
		List<SprintDetails> toBeSavedSprintDetails = new ArrayList<>();
		sprintDetailsSet.forEach(fetchedSprintDetails -> {
			SprintDetails dbSprintDetails = sprintRepository.findBySprintID(fetchedSprintDetails.getSprintID());
			if (Objects.isNull(dbSprintDetails)) {
				// first time run and fetch sprint wise issues and
				// initialize issues into completed , notCompleted , total issues bucket
				log.info("fetched sprint Name -> {} sprint state -> {} ", fetchedSprintDetails.getSprintName(),
						fetchedSprintDetails.getState());
				List<SprintIssue> fetchedSprintWiseIssues = fetchAndPrepareSprintIssue(azureAdapter, azureServer,
						projectConfig, fetchedSprintDetails);
				Set<SprintIssue> completedIssues = new HashSet<>();
				Set<SprintIssue> notCompletedIssues = new HashSet<>();
				Set<SprintIssue> totalIssues = new HashSet<>();
				prepareCompletedAndNotCompletedSprintIssue(completedIssuesStatus, fetchedSprintWiseIssues,
						completedIssues, notCompletedIssues, totalIssues);
				fetchedSprintDetails.setCompletedIssues(completedIssues);
				fetchedSprintDetails.setNotCompletedIssues(notCompletedIssues);
				fetchedSprintDetails.setTotalIssues(totalIssues);
				fetchedSprintDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
				fetchedSprintDetails.setProcessorId(azureProcessorId);
				toBeSavedSprintDetails.add(fetchedSprintDetails);

			} else {
				// active sprint
				if ((fetchedSprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE)
						|| fetchedSprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED))
						&& dbSprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE)) {
					List<SprintIssue> fetchedSprintWiseIssues = fetchAndPrepareSprintIssue(azureAdapter, azureServer,
							projectConfig, fetchedSprintDetails);

					Set<SprintIssue> toBeSavedCompletedIssues = new HashSet<>();
					Set<SprintIssue> toBeSavedNotCompletedIssues = new HashSet<>();
					Set<SprintIssue> toBeSavedTotalIssues = new HashSet<>();
					Set<String> toBeSavedAddedIssues = new HashSet<>();
					Set<SprintIssue> toBeSavedPuntedIssues = new HashSet<>();

					prepareCompletedAndNotCompletedSprintIssue(completedIssuesStatus, fetchedSprintWiseIssues,
							toBeSavedCompletedIssues, toBeSavedNotCompletedIssues, toBeSavedTotalIssues);

					prepareAddedSprintIssues(dbSprintDetails, fetchedSprintWiseIssues, toBeSavedAddedIssues);
					prepareRemoveSprintIssues(dbSprintDetails, fetchedSprintWiseIssues, toBeSavedPuntedIssues);

					fetchedSprintDetails.setCompletedIssues(toBeSavedCompletedIssues);
					fetchedSprintDetails.setNotCompletedIssues(toBeSavedNotCompletedIssues);
					fetchedSprintDetails.setTotalIssues(toBeSavedTotalIssues);
					fetchedSprintDetails.setAddedIssues(toBeSavedAddedIssues);
					fetchedSprintDetails.setPuntedIssues(toBeSavedPuntedIssues);

					fetchedSprintDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
					fetchedSprintDetails.setProcessorId(azureProcessorId);
					fetchedSprintDetails.setId(dbSprintDetails.getId());
					toBeSavedSprintDetails.add(fetchedSprintDetails);

				}

				if (fetchedSprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE)
						&& dbSprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_FUTURE)) {
					// update all issues before sprint future sprint start
					List<SprintIssue> fetchedSprintWiseIssues = fetchAndPrepareSprintIssue(azureAdapter, azureServer,
							projectConfig, fetchedSprintDetails);

					Set<SprintIssue> toBeSavedCompletedIssues = new HashSet<>();
					Set<SprintIssue> toBeSavedNotCompletedIssues = new HashSet<>();
					Set<SprintIssue> toBeSavedTotalIssues = new HashSet<>();
					prepareCompletedAndNotCompletedSprintIssue(completedIssuesStatus, fetchedSprintWiseIssues,
							toBeSavedCompletedIssues, toBeSavedNotCompletedIssues, toBeSavedTotalIssues);

					fetchedSprintDetails.setCompletedIssues(toBeSavedCompletedIssues);
					fetchedSprintDetails.setNotCompletedIssues(toBeSavedNotCompletedIssues);
					fetchedSprintDetails.setTotalIssues(toBeSavedTotalIssues);

					fetchedSprintDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
					fetchedSprintDetails.setProcessorId(azureProcessorId);
					fetchedSprintDetails.setId(dbSprintDetails.getId());
					toBeSavedSprintDetails.add(fetchedSprintDetails);
				}
			}
		});
		sprintRepository.saveAll(toBeSavedSprintDetails);

		basedOnIterationStatusUpdateSprintReportIssuesShuffle(projectConfig);
	}

	/**
	 *
	 * @param completedIssuesStatus
	 * @param fetchedSprintWiseIssues
	 * @param toBeSavedCompletedIssues
	 * @param toBeSavedNotCompletedIssues
	 * @param toBeSavedTotalIssues
	 */
	private void prepareCompletedAndNotCompletedSprintIssue(List<String> completedIssuesStatus,
			List<SprintIssue> fetchedSprintWiseIssues, Set<SprintIssue> toBeSavedCompletedIssues,
			Set<SprintIssue> toBeSavedNotCompletedIssues, Set<SprintIssue> toBeSavedTotalIssues) {

		fetchedSprintWiseIssues.forEach(sprintIssue -> {
			if (completedIssuesStatus.contains(sprintIssue.getStatus())) {
				toBeSavedCompletedIssues.add(sprintIssue);
				toBeSavedTotalIssues.add(sprintIssue);
			} else {
				toBeSavedNotCompletedIssues.add(sprintIssue);
				toBeSavedTotalIssues.add(sprintIssue);
			}
		});
	}

	/**
	 *
	 * @param dbSprintDetails
	 * @param fetchedSprintWiseIssues
	 * @param toBeSavedPuntedIssues
	 */

	private void prepareRemoveSprintIssues(SprintDetails dbSprintDetails, List<SprintIssue> fetchedSprintWiseIssues,
			Set<SprintIssue> toBeSavedPuntedIssues) {
		if (CollectionUtils.isNotEmpty(dbSprintDetails.getTotalIssues())) {
			List<String> fetchedTotalIssues = fetchedSprintWiseIssues.stream().filter(Objects::nonNull)
					.map(SprintIssue::getNumber).distinct().collect(Collectors.toList());

			List<SprintIssue> removedIssues = dbSprintDetails.getTotalIssues().stream().filter(Objects::nonNull)
					.filter(sprintIssue -> !fetchedTotalIssues.contains(sprintIssue.getNumber())).distinct()
					.collect(Collectors.toList());
			toBeSavedPuntedIssues.addAll(dbSprintDetails.getPuntedIssues());
			toBeSavedPuntedIssues.addAll(removedIssues);
		}
	}

	/**
	 *
	 * @param dbSprintDetails
	 * @param fetchedSprintWiseIssues
	 * @param toBeSavedAddedIssues
	 */
	private void prepareAddedSprintIssues(SprintDetails dbSprintDetails, List<SprintIssue> fetchedSprintWiseIssues,
			Set<String> toBeSavedAddedIssues) {
		if (CollectionUtils.isNotEmpty(dbSprintDetails.getTotalIssues())) {
			List<String> dbTotalIssues = dbSprintDetails.getTotalIssues().stream().filter(Objects::nonNull)
					.map(SprintIssue::getNumber).distinct().collect(Collectors.toList());
			Set<String> addedIssues = fetchedSprintWiseIssues.stream().filter(Objects::nonNull)
					.filter(sprintIssue -> !dbTotalIssues.contains(sprintIssue.getNumber())).map(SprintIssue::getNumber)
					.collect(Collectors.toSet());
			toBeSavedAddedIssues.addAll(dbSprintDetails.getAddedIssues());
			toBeSavedAddedIssues.addAll(addedIssues);
		}
	}

	/**
	 *
	 * @param projectConfig
	 */
	private void basedOnIterationStatusUpdateSprintReportIssuesShuffle(ProjectConfFieldMapping projectConfig) {
		FieldMapping fieldMapping = projectConfig.getFieldMapping();
		ProjectToolConfig projectToolConfig = projectToolConfigRepository
				.findById(projectConfig.getAzureBoardToolConfigId().toString());
		if (projectToolConfig.isAzureIterationStatusFieldUpdate()
				&& CollectionUtils.isNotEmpty(fieldMapping.getJiraIterationCompletionStatusCustomField())) {
			List<SprintDetails> dbSprintDetailsList = sprintRepository
					.findByBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
			dbSprintDetailsList.stream().forEach(sprintDetails -> {
				if (CollectionUtils.isNotEmpty(sprintDetails.getTotalIssues())) {
					Set<SprintIssue> toBeSavedCompletedIssues = new HashSet<>();
					Set<SprintIssue> toBeSavedNotCompletedIssues = new HashSet<>();
					sprintDetails.getTotalIssues().stream().forEach(sprintIssue -> {
						if (fieldMapping.getJiraIterationCompletionStatusCustomField()
								.contains(sprintIssue.getStatus())) {
							toBeSavedCompletedIssues.add(sprintIssue);
						} else {
							toBeSavedNotCompletedIssues.add(sprintIssue);
						}
					});
					sprintDetails.setCompletedIssues(toBeSavedCompletedIssues);
					sprintDetails.setNotCompletedIssues(toBeSavedNotCompletedIssues);
				}
			});
			sprintRepository.saveAll(dbSprintDetailsList);
			projectToolConfig.setAzureIterationStatusFieldUpdate(false);
			projectToolConfigRepository.save(projectToolConfig);
		}
	}

	/**
	 *
	 * @param azureAdapter
	 * @param azureServer
	 * @param projectConfig
	 * @param fetchedSprintDetails
	 * @return
	 */
	private List<SprintIssue> fetchAndPrepareSprintIssue(AzureAdapter azureAdapter, AzureServer azureServer,
			ProjectConfFieldMapping projectConfig, SprintDetails fetchedSprintDetails) {
		List<String> sprintWiseItemIdList = azureAdapter.getIssuesBySprint(azureServer,
				fetchedSprintDetails.getOriginalSprintId());
		List<String> sprintWiseIssueList = new ArrayList<>();
		List<SprintIssue> sprintIssueList = new ArrayList<>();
		sprintWiseItemIdList.stream().forEach(id -> sprintWiseIssueList.add(getModifiedIssueId(projectConfig, id)));
		List<JiraIssue> jiraIssueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(sprintWiseIssueList,
				projectConfig.getBasicProjectConfigId().toString());
		if (CollectionUtils.isNotEmpty(jiraIssueList)) {
			jiraIssueList.stream().forEach(jiraIssue -> sprintIssueList.add(setSprintIssueDetails(jiraIssue)));
		}
		log.info("fetched sprint name -> {} Issues -> {} ", fetchedSprintDetails.getSprintName(), sprintWiseItemIdList);
		return sprintIssueList;
	}

	/**
	 * 
	 * @param azureIssue
	 * @return
	 */
	private SprintIssue setSprintIssueDetails(JiraIssue azureIssue) {
		SprintIssue sprintIssue = new SprintIssue();
		sprintIssue.setNumber(azureIssue.getNumber());
		sprintIssue.setTypeName(azureIssue.getTypeName());
		sprintIssue.setStatus(azureIssue.getStatus());
		sprintIssue.setPriority(azureIssue.getPriority());
		if (azureIssue.getStoryPoints() != null)
			sprintIssue.setStoryPoints(azureIssue.getStoryPoints());
		if (azureIssue.getEstimate() != null)
			sprintIssue.setOriginalEstimate(Double.valueOf(azureIssue.getEstimate()));
		return sprintIssue;
	}

	private String getModifiedIssueId(ProjectConfFieldMapping projectConfig, String issueId) {
		StringBuilder projectKeyIssueId = new StringBuilder(projectConfig.getProjectKey());
		projectKeyIssueId.append("-").append(issueId);
		return projectKeyIssueId.toString();
	}
}
