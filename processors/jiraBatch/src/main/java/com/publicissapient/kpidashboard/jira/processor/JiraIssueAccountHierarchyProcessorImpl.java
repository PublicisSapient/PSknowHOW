package com.publicissapient.kpidashboard.jira.processor;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JiraIssueAccountHierarchyProcessorImpl implements JiraIssueAccountHierarchyProcessor {

	@Autowired
	private HierarchyLevelService hierarchyLevelService;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;

	HierarchyLevel sprintHierarchyLevel;
	List<String> additionalFilterCategoryIds;
	Map<Pair<String, String>, AccountHierarchy> existingHierarchy;

	@Override
	public Set<AccountHierarchy> createAccountHierarchy(JiraIssue jiraIssue, ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet) {

		if (null == sprintHierarchyLevel) {
			List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
					.getFullHierarchyLevels(projectConfig.isKanban());

			Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
					.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));

			sprintHierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);

			additionalFilterCategoryIds = hierarchyLevelList.stream()
					.filter(x -> x.getLevel() > sprintHierarchyLevel.getLevel())
					.map(HierarchyLevel::getHierarchyLevelId).collect(Collectors.toList());
		}

		if (null == existingHierarchy) {
			log.info("Fetching all hierarchy levels");
			List<AccountHierarchy> accountHierarchyList = accountHierarchyRepository.findAll();
			existingHierarchy = accountHierarchyList.stream()
					.collect(Collectors.toMap(p -> Pair.of(p.getNodeId(), p.getPath()), p -> p));
		}

		Set<AccountHierarchy> setToSave = new HashSet<>();
		if (StringUtils.isNotBlank(jiraIssue.getProjectName()) && StringUtils.isNotBlank(jiraIssue.getSprintName())
				&& StringUtils.isNotBlank(jiraIssue.getSprintBeginDate())
				&& StringUtils.isNotBlank(jiraIssue.getSprintEndDate())) {

			Map<ObjectId, AccountHierarchy> projectDataMap = new HashMap<>();
			ObjectId basicProjectConfigId = new ObjectId(jiraIssue.getBasicProjectConfigId());
			Map<String, SprintDetails> sprintDetailsMap = sprintDetailsSet.stream()
					.filter(sprintDetails -> sprintDetails.getBasicProjectConfigId().equals(basicProjectConfigId))
					.collect(Collectors.toMap(sprintDetails -> sprintDetails.getSprintID().split("_")[0],
							sprintDetails -> sprintDetails));
			AccountHierarchy projectData = projectDataMap.computeIfAbsent(basicProjectConfigId, id -> {
				List<AccountHierarchy> projectDataList = accountHierarchyRepository
						.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, id);
				return projectDataList.isEmpty() ? null : projectDataList.get(0);
			});

			for (String sprintId : jiraIssue.getSprintIdList()) {
				SprintDetails sprintDetails = sprintDetailsMap.get(sprintId);

				if (sprintDetails != null) {

					AccountHierarchy sprintHierarchy = createHierarchyForSprint(sprintDetails,
							projectConfig.getProjectBasicConfig(), projectData, sprintHierarchyLevel);

					setToSaveAccountHierarchy(setToSave, sprintHierarchy, existingHierarchy);

					List<AccountHierarchy> additionalFiltersHierarchies = accountHierarchiesForAdditionalFilters(jiraIssue,
							sprintHierarchy, sprintHierarchyLevel, additionalFilterCategoryIds);
					additionalFiltersHierarchies.forEach(
							accountHierarchy -> setToSaveAccountHierarchy(setToSave, accountHierarchy, existingHierarchy));

				}
			}
		}

		return setToSave;
	}

	private void setToSaveAccountHierarchy(Set<AccountHierarchy> setToSave, AccountHierarchy accountHierarchy,
			Map<Pair<String, String>, AccountHierarchy> existingHierarchy) {
		if (StringUtils.isNotBlank(accountHierarchy.getParentId())) {
			AccountHierarchy exHiery = existingHierarchy
					.get(Pair.of(accountHierarchy.getNodeId(), accountHierarchy.getPath()));

			if (null == exHiery) {
				accountHierarchy.setCreatedDate(LocalDateTime.now());
				// update map with hierarchy to be saved
				existingHierarchy.put(Pair.of(accountHierarchy.getNodeId(), accountHierarchy.getPath()),
						accountHierarchy);
				setToSave.add(accountHierarchy);
			}
		}
	}

	private AccountHierarchy createHierarchyForSprint(SprintDetails sprintDetails, ProjectBasicConfig projectBasicConfig,
													  AccountHierarchy projectHierarchy, HierarchyLevel hierarchyLevel) {
		AccountHierarchy accountHierarchy = null;
		try {

			accountHierarchy = new AccountHierarchy();
			accountHierarchy.setBasicProjectConfigId(projectBasicConfig.getId());
			accountHierarchy.setIsDeleted(JiraConstants.FALSE);
			accountHierarchy.setLabelName(hierarchyLevel.getHierarchyLevelId());
			String sprintName = (String) PropertyUtils.getSimpleProperty(sprintDetails, "sprintName");
			String sprintId = (String) PropertyUtils.getSimpleProperty(sprintDetails, "sprintID");
			accountHierarchy.setNodeId(sprintId);
			accountHierarchy.setNodeName(sprintName + JiraConstants.COMBINE_IDS_SYMBOL +  projectBasicConfig.getProjectName());

			accountHierarchy.setPath(new StringBuffer(56).append(projectHierarchy.getNodeId())
					.append(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER).append(projectHierarchy.getPath()).toString());
			accountHierarchy.setParentId(projectHierarchy.getNodeId());

		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			log.error("Jira Processor Failed to get Account Hierarchy data {}", e);
		}
		return accountHierarchy;
	}

	private List<AccountHierarchy> accountHierarchiesForAdditionalFilters(JiraIssue jiraIssue,
			AccountHierarchy sprintHierarchy, HierarchyLevel sprintHierarchyLevel,
			List<String> additionalFilterCategoryIds) {

		List<AccountHierarchy> accountHierarchies = new ArrayList<>();
		List<AdditionalFilter> additionalFilters = ListUtils.emptyIfNull(jiraIssue.getAdditionalFilters());

		additionalFilters.forEach(additionalFilter -> {
			if (additionalFilterCategoryIds.contains(additionalFilter.getFilterId())) {
				String labelName = additionalFilter.getFilterId();
				additionalFilter.getFilterValues().forEach(additionalFilterValue -> {
					AccountHierarchy adFilterAccountHierarchy = new AccountHierarchy();
					adFilterAccountHierarchy.setLabelName(labelName);
					adFilterAccountHierarchy.setNodeId(additionalFilterValue.getValueId());
					adFilterAccountHierarchy.setNodeName(additionalFilterValue.getValue());
					adFilterAccountHierarchy.setParentId(sprintHierarchy.getNodeId());
					adFilterAccountHierarchy.setPath(sprintHierarchy.getNodeId()
							+ CommonConstant.ACC_HIERARCHY_PATH_SPLITTER + sprintHierarchy.getPath());
					adFilterAccountHierarchy.setBasicProjectConfigId(new ObjectId(jiraIssue.getBasicProjectConfigId()));
					accountHierarchies.add(adFilterAccountHierarchy);
				});
			}

		});

		return accountHierarchies;
	}

	@Override
	public void cleanAllObjects() {
		sprintHierarchyLevel = null;
		additionalFilterCategoryIds = null;
		existingHierarchy = null;
	}
}
