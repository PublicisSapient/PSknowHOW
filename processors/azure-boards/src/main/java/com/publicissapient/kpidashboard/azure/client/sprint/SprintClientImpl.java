package com.publicissapient.kpidashboard.azure.client.sprint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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
	 * all sprint issues saved based on status configure field mapping field status
	 * closed sprint issues will be not changes after saved sprint details once
	 * active sprint issues bucket changes based on comparison of dbSprintDetails vs
	 * fetched issues future sprint issues only changes while
	 *
	 * @param projectConfig
	 * @param sprintDetailsSet
	 * @param azureAdapter
	 * @param azureServer
	 */
	@Override
	public void prepareSprintReport(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet,
			AzureAdapter azureAdapter, AzureServer azureServer) throws Exception {
		FieldMapping fieldMapping = projectConfig.getFieldMapping();
		List<String> completedIssuesStatus = fieldMapping.getJiraIterationCompletionStatusCustomField();
		ObjectId azureProcessorId = azureProcessorRepository.findByProcessorName(ProcessorConstants.AZURE).getId();
		List<SprintDetails> toBeSavedSprintDetails = new ArrayList<>();
		sprintDetailsSet.forEach(fetchedSprintDetails -> {
			SprintDetails dbSprintDetails = sprintRepository.findBySprintID(fetchedSprintDetails.getSprintID());
			if (Objects.isNull(dbSprintDetails)) {
				// first time run and fetch sprint wise issues and
				// initialize issues into completed , notCompleted , total issues bucket
				log.info("fetched sprint Name -> {} , sprint state -> {} ", fetchedSprintDetails.getSprintName(),
						fetchedSprintDetails.getState());
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
				toBeSavedSprintDetails.add(fetchedSprintDetails);
				log.debug("saved sprint Id -> {} , toBeSavedCompletedIssues -> {} , toBeSavedNotCompletedIssues -> {}",
						fetchedSprintDetails.getSprintID(), getIssuesIdList(toBeSavedCompletedIssues),
						getIssuesIdList(toBeSavedNotCompletedIssues));

			} else {
				// fetched and db sprint is active then issues bucket compare as per
				if (fetchedSprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE)
						&& dbSprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE)) {
					log.debug(
							"DB Active Sprint State -> {} , dbCompletedIssues -> {} , dbNotCompletedIssues -> {} , dbAddedIssues -> {} , dbPuntedIssues -> {}",
							fetchedSprintDetails.getSprintID(), getIssuesIdList(dbSprintDetails.getCompletedIssues()),
							getIssuesIdList(dbSprintDetails.getNotCompletedIssues()), dbSprintDetails.getAddedIssues(),
							getIssuesIdList(dbSprintDetails.getPuntedIssues()));
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
					preparePuntedSprintIssues(dbSprintDetails, fetchedSprintWiseIssues, toBeSavedPuntedIssues);

					fetchedSprintDetails.setCompletedIssues(toBeSavedCompletedIssues);
					fetchedSprintDetails.setNotCompletedIssues(toBeSavedNotCompletedIssues);
					fetchedSprintDetails.setTotalIssues(toBeSavedTotalIssues);
					fetchedSprintDetails.setAddedIssues(toBeSavedAddedIssues);
					fetchedSprintDetails.setPuntedIssues(toBeSavedPuntedIssues);

					fetchedSprintDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
					fetchedSprintDetails.setProcessorId(azureProcessorId);
					fetchedSprintDetails.setId(dbSprintDetails.getId());
					toBeSavedSprintDetails.add(fetchedSprintDetails);
					log.debug(
							"Active sprint Id -> {} , toBeSavedCompletedIssues -> {} , toBeSavedNotCompletedIssues -> {}",
							fetchedSprintDetails.getSprintID(), getIssuesIdList(toBeSavedCompletedIssues),
							getIssuesIdList(toBeSavedNotCompletedIssues));
				}

				if (fetchedSprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED)
						&& dbSprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE)) {
					// fetched sprint closed and after that all not completed issues tag to next
					// sprint/backlog so that
					// snapshot of based on last active state and fetched completed issues from
					// fetched sprint

					List<SprintIssue> fetchedSprintWiseIssues = fetchAndPrepareSprintIssue(azureAdapter, azureServer,
							projectConfig, fetchedSprintDetails);

					Set<SprintIssue> toBeSavedCompletedIssues = new HashSet<>();
					Set<SprintIssue> toBeSavedNotCompletedIssues = new HashSet<>();
					Set<SprintIssue> toBeSavedTotalIssues = new HashSet<>();

					prepareCompletedAndNotCompletedSprintIssue(completedIssuesStatus, fetchedSprintWiseIssues,
							toBeSavedCompletedIssues, toBeSavedNotCompletedIssues, toBeSavedTotalIssues);

					toBeSavedCompletedIssues.addAll(dbSprintDetails.getCompletedIssues());
					toBeSavedTotalIssues.addAll(dbSprintDetails.getTotalIssues());

					Set<SprintIssue> dbNotCompletedIssues = dbSprintDetails.getNotCompletedIssues();
					dbNotCompletedIssues.removeAll(toBeSavedCompletedIssues);

					fetchedSprintDetails.setCompletedIssues(toBeSavedCompletedIssues);
					fetchedSprintDetails.setNotCompletedIssues(dbNotCompletedIssues);
					fetchedSprintDetails.setTotalIssues(toBeSavedTotalIssues);
					fetchedSprintDetails.setAddedIssues(dbSprintDetails.getAddedIssues());
					fetchedSprintDetails.setPuntedIssues(dbSprintDetails.getPuntedIssues());

					fetchedSprintDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
					fetchedSprintDetails.setProcessorId(azureProcessorId);
					fetchedSprintDetails.setId(dbSprintDetails.getId());
					toBeSavedSprintDetails.add(fetchedSprintDetails);
					log.debug(
							"fetched sprint Id -> {} , toBeSavedCompletedIssues -> {} , toBeSavedNotCompletedIssues -> {}",
							fetchedSprintDetails.getSprintID(), getIssuesIdList(toBeSavedCompletedIssues),
							getIssuesIdList(toBeSavedNotCompletedIssues));

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
					log.debug(
							"fetched sprint Id -> {} , toBeSavedCompletedIssues -> {} , toBeSavedNotCompletedIssues -> {}",
							fetchedSprintDetails.getSprintID(), getIssuesIdList(toBeSavedCompletedIssues),
							getIssuesIdList(toBeSavedNotCompletedIssues));
				}
			}
		});

		sprintRepository.saveAll(toBeSavedSprintDetails);

		iterationStatusUpdateSPIssuesShuffle(projectConfig, completedIssuesStatus);
	}

	/**
	 * based on field mapping completed issues status divided into completed and not
	 * completed issues bucket
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
			if (CollectionUtils.isNotEmpty(completedIssuesStatus)
					&& completedIssuesStatus.contains(sprintIssue.getStatus())) {
				toBeSavedCompletedIssues.add(sprintIssue);
				toBeSavedTotalIssues.add(sprintIssue);
			} else {
				toBeSavedNotCompletedIssues.add(sprintIssue);
				toBeSavedTotalIssues.add(sprintIssue);
			}
		});
	}

	/**
	 * if any fetched issues not found in db total issues then count into removed
	 * issues for active sprint
	 *
	 * @param dbSprintDetails
	 * @param fetchedSprintWiseIssues
	 * @param toBeSavedPuntedIssues
	 */

	private void preparePuntedSprintIssues(SprintDetails dbSprintDetails, List<SprintIssue> fetchedSprintWiseIssues,
			Set<SprintIssue> toBeSavedPuntedIssues) {
		if (CollectionUtils.isNotEmpty(dbSprintDetails.getTotalIssues())) {
			List<String> fetchedTotalIssues = fetchedSprintWiseIssues.stream().filter(Objects::nonNull)
					.map(SprintIssue::getNumber).distinct().collect(Collectors.toList());

			Set<SprintIssue> removedIssues = dbSprintDetails.getTotalIssues().stream().filter(Objects::nonNull)
					.filter(sprintIssue -> !fetchedTotalIssues.contains(sprintIssue.getNumber()))
					.collect(Collectors.toSet());
			if (CollectionUtils.isNotEmpty(dbSprintDetails.getPuntedIssues())) {
				toBeSavedPuntedIssues.addAll(dbSprintDetails.getPuntedIssues());
			}
			toBeSavedPuntedIssues.addAll(removedIssues);
			log.debug("Active Sprint Name -> {} , fetched total issues -> {} , removed issues -> {} ",
					dbSprintDetails.getSprintName(), fetchedTotalIssues, getIssuesIdList(removedIssues));
		}
	}

	/**
	 * if db total issues and fetched total issues diff is added issue for active
	 * sprint
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
			if (CollectionUtils.isNotEmpty(dbSprintDetails.getAddedIssues())) {
				toBeSavedAddedIssues.addAll(dbSprintDetails.getAddedIssues());
			}
			toBeSavedAddedIssues.addAll(addedIssues);

			// if any issues removed from sprint and with in same sprint added same issues
			// added in sprint then it will be not count in punted and added list ,
			// count in total list.
			if (CollectionUtils.isNotEmpty(dbSprintDetails.getPuntedIssues())
					&& CollectionUtils.isNotEmpty(addedIssues)) {
				Set<SprintIssue> dbPuntedIssues = dbSprintDetails.getPuntedIssues();
				Set<SprintIssue> puntedAndAddedWithInSprint = dbPuntedIssues.stream().filter(Objects::nonNull)
						.filter(sprintIssue -> addedIssues.contains(sprintIssue.getNumber()))
						.collect(Collectors.toSet());
				List<String> puntedAndAddedIds = getIssuesIdList(puntedAndAddedWithInSprint);
				dbPuntedIssues.removeAll(puntedAndAddedWithInSprint);
				dbSprintDetails.setPuntedIssues(dbPuntedIssues);
				toBeSavedAddedIssues.removeAll(puntedAndAddedIds);
				log.debug("Sprint Id -> {} , puntedAndAddedIds -> {}", dbSprintDetails.getSprintID(),
						puntedAndAddedIds);
			}

			log.debug("Active Sprint Name -> {} , db total issues -> {} , added issues -> {} ",
					dbSprintDetails.getSprintName(), dbTotalIssues, addedIssues);
		}
	}

	/**
	 * if field mapping completedIssuesStatus field is changes then existing sprint
	 * report issues will be shuffled as per status
	 * 
	 * @param projectConfig
	 * @param completedIssuesStatus
	 */
	private void iterationStatusUpdateSPIssuesShuffle(ProjectConfFieldMapping projectConfig,
			List<String> completedIssuesStatus) {
		ProjectToolConfig projectToolConfig = projectToolConfigRepository
				.findById(projectConfig.getAzureBoardToolConfigId().toString());
		if (projectToolConfig.isAzureIterationStatusFieldUpdate()
				&& CollectionUtils.isNotEmpty(completedIssuesStatus)) {
			List<SprintDetails> dbSprintDetailsList = sprintRepository
					.findByBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
			dbSprintDetailsList.stream().forEach(sprintDetails -> {
				if (CollectionUtils.isNotEmpty(sprintDetails.getTotalIssues())) {
					Set<SprintIssue> toBeSavedCompletedIssues = new HashSet<>();
					Set<SprintIssue> toBeSavedNotCompletedIssues = new HashSet<>();
					sprintDetails.getTotalIssues().stream().forEach(sprintIssue -> {
						if (completedIssuesStatus.contains(sprintIssue.getStatus())) {
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
			log.debug("sprint issues shuffle based on completedIssuesStatus field update -> ",
					projectToolConfig.getBasicProjectConfigId());
		}
	}

	/**
	 * fetched items sprint wise and convert into sprint issue
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
		log.info("fetched sprint Id -> {} , Issues -> {} ", fetchedSprintDetails.getSprintID(), sprintWiseItemIdList);
		return sprintIssueList;
	}

	/**
	 * azure item details fetched from jira issues and convert into sprint issue
	 * 
	 * @param azureIssue
	 * @return SprintIssue
	 */
	private SprintIssue setSprintIssueDetails(JiraIssue azureIssue) {
		SprintIssue sprintIssue = new SprintIssue();
		sprintIssue.setNumber(azureIssue.getNumber());
		sprintIssue.setTypeName(azureIssue.getTypeName());
		sprintIssue.setStatus(azureIssue.getStatus());
		sprintIssue.setPriority(azureIssue.getPriority());
		if (azureIssue.getStoryPoints() != null)
			sprintIssue.setStoryPoints(azureIssue.getStoryPoints());
		return sprintIssue;
	}

	private String getModifiedIssueId(ProjectConfFieldMapping projectConfig, String issueId) {
		StringBuilder projectKeyIssueId = new StringBuilder(projectConfig.getProjectKey());
		projectKeyIssueId.append("-").append(issueId);
		return projectKeyIssueId.toString();
	}

	private List<String> getIssuesIdList(Set<SprintIssue> sprintIssueSet) {
		if (CollectionUtils.isNotEmpty(sprintIssueSet)) {
			return sprintIssueSet.stream().filter(Objects::nonNull).map(SprintIssue::getNumber)
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
}
