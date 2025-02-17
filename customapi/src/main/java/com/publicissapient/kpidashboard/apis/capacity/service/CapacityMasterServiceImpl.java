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

package com.publicissapient.kpidashboard.apis.capacity.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.SprintDetailsService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCapacity;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.AssigneeCapacity;
import com.publicissapient.kpidashboard.common.model.application.CapacityMaster;
import com.publicissapient.kpidashboard.common.model.application.LeafNodeCapacity;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Week;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.UserRatingData;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.excel.KanbanCapacityRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author narsingh9
 */
@Service
@Slf4j
public class CapacityMasterServiceImpl implements CapacityMasterService {

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	private static final String SPRINT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	@Autowired
	private CapacityKpiDataRepository capacityKpiDataRepository;

	@Autowired
	private KanbanCapacityRepository kanbanCapacityRepository;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private KpiDataCacheService kpiDataCacheService;

	@Autowired
	private ProjectBasicConfigService projectBasicConfigService;

	@Autowired
	private SprintDetailsService sprintDetailsService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private HappinessKpiDataRepository happinessKpiDataRepository;

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Autowired
	private FilterHelperService filterHelperService;

	/**
	 * This method process the capacity data.
	 *
	 * @param capacityMaster
	 *            capacityMaster
	 * @return CapacityMaster object
	 */
	@Override
	public CapacityMaster processCapacityData(CapacityMaster capacityMaster) {
		boolean saved;
		saveAssigneEmail(capacityMaster);
		if (capacityMaster.isKanban()) {
			saved = processKanbanTeamCapacityData(capacityMaster);
		} else {
			saved = processSprintCapacityData(capacityMaster);
		}

		if (!saved) {
			capacityMaster = null;
		}
		return capacityMaster;
	}

	private void saveAssigneEmail(CapacityMaster capacityMaster) {
		AssigneeDetails assigneeDetails = assigneeDetailsRepository
				.findByBasicProjectConfigId(capacityMaster.getBasicProjectConfigId().toString());
		List<CapacityKpiData> capacities = (List<CapacityKpiData>) capacityKpiDataRepository.findAll();
		if (assigneeDetails == null)
			return;
		Set<Assignee> assignee = assigneeDetails.getAssignee();
		Map<String, Assignee> map = new HashMap<>();
		if (assignee != null && capacityMaster.getAssigneeCapacity() != null) {
			assignee.forEach(assignee1 -> map.put(assignee1.getAssigneeId(), assignee1));
			capacityMaster.getAssigneeCapacity().forEach(capacity -> {
				Assignee assignee1 = map.get(capacity.getUserId());
				if (capacity.getEmail() != null)
					assignee1.setEmail(capacity.getEmail());
				map.put(assignee1.getAssigneeId(), assignee1);
			});
			Set<Assignee> finalAssignee = new HashSet<>();
			map.forEach((key, value) -> finalAssignee.add(value));
			assigneeDetails.setAssignee(finalAssignee);
			assigneeDetailsRepository.save(assigneeDetails);

			capacities.forEach(capacityKpiData1 -> {
				if (capacityKpiData1.getAssigneeCapacity() != null) {
					capacityKpiData1.getAssigneeCapacity().forEach(ass -> {
						Assignee assignee1 = map.get(ass.getUserId());
						if (assignee1 != null && assignee1.getEmail() != null)
							ass.setEmail(assignee1.getEmail());
					});
					capacityKpiDataRepository.save(capacityKpiData1);
				}
			});
		}
		cacheService.clearCache(CommonConstant.BITBUCKET_KPI_CACHE);
	}

	@Override
	public List<CapacityMaster> getCapacities(String basicProjectConfigId) {
		// find the project is scrum or kanban
		ProjectBasicConfig project = projectBasicConfigService.getProjectBasicConfigs(basicProjectConfigId);
		List<CapacityMaster> capacityMasterList = new ArrayList<>();
		if (project != null) {
			if (project.getIsKanban()) {
				capacityMasterList.addAll(getCapacityDataForKanban(project));
			} else {
				capacityMasterList.addAll(getCapacityDataForScrum(project));
			}
		}

		log.info("capacity data size = " + capacityMasterList.size());
		return capacityMasterList;
	}

	private List<CapacityMaster> getCapacityDataForScrum(ProjectBasicConfig project) {

		List<CapacityMaster> capacityMasterList = new ArrayList<>();
		List<SprintDetails> sprintDetails = filterSprints(
				sprintDetailsService.getSprintDetails(project.getId().toHexString()));
		List<String> sprintIds = sprintDetails.stream().map(SprintDetails::getSprintID).collect(Collectors.toList());
		List<CapacityKpiData> capacityKpiDataList = capacityKpiDataRepository.findBySprintIDIn(sprintIds);
		List<HappinessKpiData> happinessKpiDataList = happinessKpiDataRepository.findBySprintIDIn(sprintIds);
		List<AssigneeCapacity> assigneeCapacityList = null;
		for (SprintDetails sprint : sprintDetails) {
			CapacityKpiData capacityKpiData = capacityKpiDataList.stream()
					.filter(capacityData -> capacityData.getSprintID().equals(sprint.getSprintID())).findAny()
					.orElse(null);

			// finding latest submitted happiness index data for a sprint
			HappinessKpiData happinessKpiData = happinessKpiDataList.stream()
					.filter(data -> data.getSprintID().equals(sprint.getSprintID()))
					.sorted(Comparator.comparing(HappinessKpiData::getDateOfSubmission).reversed()).findFirst()
					.orElse(null);

			CapacityMaster capacityMaster = new CapacityMaster();

			if (capacityKpiData != null) {
				capacityMaster.setId(capacityKpiData.getId());
				capacityMaster.setCapacity(Math.round(capacityKpiData.getCapacityPerSprint() * 100) / 100.0);
				capacityMaster.setAdditionalFilterCapacityList(capacityKpiData.getAdditionalFilterCapacityList());
				if (CollectionUtils.isNotEmpty(capacityKpiData.getAssigneeCapacity())
						&& project.isSaveAssigneeDetails()) {
					capacityKpiData.getAssigneeCapacity().stream().forEach(assigneeCapacity -> assigneeCapacity
							.setLeaves(Optional.ofNullable(assigneeCapacity.getLeaves()).orElse(0D)));
					capacityKpiData.getAssigneeCapacity().stream()
							.forEach(assigneeCapacity -> assigneeCapacity.setHappinessRating(0));
					// Setting most recently submitted happiness index value for a sprint
					setHappinessIndex(happinessKpiData, capacityKpiData.getAssigneeCapacity());
					capacityMaster.setAssigneeCapacity(capacityKpiData.getAssigneeCapacity());
					// if in normal flow assignees found saving it for future
					assigneeCapacityList = createAssigneeData(capacityKpiData.getAssigneeCapacity());

				}
			} else {
				capacityMaster.setId(null);
				capacityMaster.setCapacity(0D);
			}
			settingFutureAssigneeDetails(assigneeCapacityList, capacityMaster);
			capacityMaster.setSprintName(sprint.getSprintName());
			capacityMaster.setSprintState(sprint.getState());
			capacityMaster.setSprintNodeId(sprint.getSprintID());

			setProjectMeta(capacityMaster, project);

			capacityMasterList.add(capacityMaster);

		}
		// reversing the list to show future->active->closed
		Collections.reverse(capacityMasterList);
		return capacityMasterList;
	}

	private void setHappinessIndex(HappinessKpiData happinessKpiData, List<AssigneeCapacity> assigneeCapacityList) {
		// Setting happiness index value for each assignee
		if (Objects.nonNull(happinessKpiData) && CollectionUtils.isNotEmpty(happinessKpiData.getUserRatingList())) {
			assigneeCapacityList.forEach(assigneeCapacity -> {
				UserRatingData userRatingData = happinessKpiData.getUserRatingList().stream()
						.filter(data -> data.getUserId().equals(assigneeCapacity.getUserId())).findFirst().orElse(null);
				if (Objects.nonNull(userRatingData)) {
					assigneeCapacity.setHappinessRating(userRatingData.getRating());
				}
			});

		}
	}

	private List<SprintDetails> filterSprints(List<SprintDetails> allSprints) {
		List<SprintDetails> sprints = new ArrayList<>();
		List<SprintDetails> closedSprints = new ArrayList<>();

		List<SprintDetails> sortedClosedSprints = allSprints.stream()
				.filter(sprintDetails -> SprintDetails.SPRINT_STATE_CLOSED.equalsIgnoreCase(sprintDetails.getState()))
				.sorted(Comparator.comparing((SprintDetails sprintDetails) -> LocalDateTime
						.parse(sprintDetails.getStartDate(), DateTimeFormatter.ofPattern(SPRINT_DATE_FORMAT))))
				.collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(sortedClosedSprints)) {
			closedSprints = sortedClosedSprints.subList(
					Math.max(sortedClosedSprints.size() - customApiConfig.getSprintCountForFilters(), 0),
					sortedClosedSprints.size());
		}

		List<SprintDetails> activeSprints = allSprints.stream()
				.filter(sprintDetails -> SprintDetails.SPRINT_STATE_ACTIVE.equalsIgnoreCase(sprintDetails.getState()))
				.collect(Collectors.toList());

		List<SprintDetails> futureSprints = allSprints.stream()
				.filter(sprintDetails -> SprintDetails.SPRINT_STATE_FUTURE.equalsIgnoreCase(sprintDetails.getState()))
				.collect(Collectors.toList());

		// creating list in normal order--closed(start time wise)->active->futures
		if (CollectionUtils.isNotEmpty(closedSprints)) {
			sprints.addAll(closedSprints);
		}
		if (CollectionUtils.isNotEmpty(activeSprints)) {
			sprints.addAll(activeSprints);
		}
		if (CollectionUtils.isNotEmpty(futureSprints)) {
			sprints.addAll(futureSprints);
		}

		return sprints;
	}

	private List<CapacityMaster> getCapacityDataForKanban(ProjectBasicConfig project) {
		List<CapacityMaster> capacityMasterList = new ArrayList<>();
		List<AssigneeCapacity> assigneeCapacity = null;
		List<KanbanCapacity> kanbanCapacitiesAll = kanbanCapacityRepository.findByBasicProjectConfigId(project.getId());
		List<Week> weeksToShow = getWeeksToShow();

		for (Week week : weeksToShow) {
			CapacityMaster capacityMasterKanban = new CapacityMaster();

			if (CollectionUtils.isNotEmpty(kanbanCapacitiesAll)) {
				KanbanCapacity kanbanCapacity = kanbanCapacitiesAll.stream()
						.filter(capacity -> week.getStartDate().equals(capacity.getStartDate())).findAny().orElse(null);
				if (kanbanCapacity != null) {
					capacityMasterKanban.setId(kanbanCapacity.getId());
					// mutiplying by working days of week
					capacityMasterKanban.setCapacity(kanbanCapacity.getCapacity() * 5);
					var additionalFilterCapacityList = kanbanCapacity.getAdditionalFilterCapacityList();
					calculateAdditinalFilterCapacityForKanban(additionalFilterCapacityList);
					capacityMasterKanban.setAdditionalFilterCapacityList(additionalFilterCapacityList);
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
					capacityMasterKanban.setStartDate(kanbanCapacity.getStartDate().format(formatter));
					capacityMasterKanban.setEndDate(kanbanCapacity.getEndDate().format(formatter));
					if (CollectionUtils.isNotEmpty(kanbanCapacity.getAssigneeCapacity())
							&& project.isSaveAssigneeDetails()) {
						kanbanCapacity.getAssigneeCapacity().stream().forEach(assignees -> assignees
								.setLeaves(Optional.ofNullable(assignees.getLeaves()).orElse(0D)));
						capacityMasterKanban.setAssigneeCapacity(kanbanCapacity.getAssigneeCapacity());
						assigneeCapacity = createAssigneeData(kanbanCapacity.getAssigneeCapacity());
					}
				} else {
					capacityMasterKanban.setId(null);
					capacityMasterKanban.setCapacity(0D);
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
					capacityMasterKanban.setStartDate(week.getStartDate().format(formatter));
					capacityMasterKanban.setEndDate(week.getEndDate().format(formatter));
				}

			} else {
				capacityMasterKanban.setId(null);
				capacityMasterKanban.setCapacity(0D);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
				capacityMasterKanban.setStartDate(week.getStartDate().format(formatter));
				capacityMasterKanban.setEndDate(week.getEndDate().format(formatter));
			}
			settingFutureAssigneeDetails(assigneeCapacity, capacityMasterKanban);
			setProjectMeta(capacityMasterKanban, project);

			capacityMasterList.add(capacityMasterKanban);

		}
		return capacityMasterList;
	}

	private void calculateAdditinalFilterCapacityForKanban(
			List<AdditionalFilterCapacity> additionalFilterCapacityList) {
		if (CollectionUtils.isNotEmpty(additionalFilterCapacityList)) {
			additionalFilterCapacityList.forEach(leafNode -> {
				var leafNodeCapacity = leafNode.getNodeCapacityList();
				if (CollectionUtils.isNotEmpty(leafNodeCapacity)) {
					leafNodeCapacity.stream().filter(capacity -> capacity.getAdditionalFilterCapacity() != null)
							.forEach(capacity -> capacity
									.setAdditionalFilterCapacity(capacity.getAdditionalFilterCapacity() * 5));
				}
			});
		}
	}

	private void settingFutureAssigneeDetails(List<AssigneeCapacity> assigneeCapacity, CapacityMaster capacityMaster) {
		if (assigneeCapacity != null && CollectionUtils.isEmpty(capacityMaster.getAssigneeCapacity())) {
			capacityMaster.setAssigneeCapacity(assigneeCapacity);
		}
	}

	private List<AssigneeCapacity> createAssigneeData(List<AssigneeCapacity> assigneeCapacity) {
		List<AssigneeCapacity> newAssigneeList = new ArrayList<>();
		assigneeCapacity.stream().forEach(assignee -> {
			AssigneeCapacity capacity = new AssigneeCapacity();
			capacity.setUserId(assignee.getUserId());
			capacity.setUserName(assignee.getUserName());
			capacity.setRole(assignee.getRole());
			capacity.setSquad(assignee.getSquad());
			capacity.setEmail(assignee.getEmail());
			capacity.setPlannedCapacity(assignee.getPlannedCapacity());
			capacity.setLeaves(0.0d);
			capacity.setHappinessRating(0);
			newAssigneeList.add(capacity);
		});
		return newAssigneeList;
	}

	private void setProjectMeta(CapacityMaster capacityMaster, ProjectBasicConfig project) {
		capacityMaster.setProjectNodeId(project.getProjectNodeId());
		capacityMaster.setProjectName(project.getProjectName());
		capacityMaster.setBasicProjectConfigId(project.getId());
		capacityMaster.setKanban(project.getIsKanban());
		capacityMaster.setAssigneeDetails(project.isSaveAssigneeDetails());
	}

	private List<Week> getWeeksToShow() {
		Week currentWeek = DateUtil.getWeek(LocalDate.now());
		List<Week> weeksToShow = new ArrayList<>();
		// current week
		weeksToShow.add(currentWeek);

		// 5 previous weeks
		LocalDate currentWeekEndDate = currentWeek.getEndDate();
		for (int i = 0; i < customApiConfig.getNumberOfPastWeeksForKanbanCapacity(); i++) {
			Week previousWeek = DateUtil.getWeek(currentWeekEndDate.minusWeeks(1));
			weeksToShow.add(previousWeek);
			currentWeekEndDate = previousWeek.getEndDate();
		}

		// 5 future weeks
		currentWeekEndDate = currentWeek.getEndDate();
		for (int i = 0; i < customApiConfig.getNumberOfFutureWeeksForKanbanCapacity(); i++) {
			Week previousWeek = DateUtil.getWeek(currentWeekEndDate.plusWeeks(1));
			weeksToShow.add(previousWeek);
			currentWeekEndDate = previousWeek.getEndDate();
		}

		return weeksToShow.stream().sorted(Comparator.comparing(Week::getStartDate)).collect(Collectors.toList());
	}

	/**
	 * process kanban team capacity data
	 *
	 * @param capacityMaster
	 *            contains data to be saved
	 * @return boolean value
	 */
	private boolean processKanbanTeamCapacityData(CapacityMaster capacityMaster) {
		boolean processed = false;
		if (capacityIdCheck(capacityMaster)) {
			List<KanbanCapacity> dataList = kanbanCapacityRepository
					.findByFilterMapAndDate(kanbanFilterMap(capacityMaster), capacityMaster.getStartDate());
			KanbanCapacity capacityData;
			if (CollectionUtils.isNotEmpty(dataList)) {
				capacityData = dataList.get(0);
				capacityData.setBasicProjectConfigId(capacityMaster.getBasicProjectConfigId());
				createKanbanAssigneeData(capacityData, capacityMaster);
			} else {
				capacityMaster.setStartDate(weekDate(capacityMaster.getStartDate(), false));
				capacityMaster.setEndDate(weekDate(capacityMaster.getStartDate(), true));
				capacityData = createKanbanCapacityData(capacityMaster);
			}

			kanbanCapacityRepository.save(capacityData);
			clearCache(CommonConstant.JIRAKANBAN_KPI_CACHE);
			processed = true;
		}
		return processed;
	}

	/**
	 * process sprint capacity data
	 *
	 * @param capacityMaster
	 *            contains data to be saved
	 * @return boolean value
	 */
	private boolean processSprintCapacityData(CapacityMaster capacityMaster) {
		boolean processed = false;
		if (capacityIdCheck(capacityMaster)) {
			List<CapacityKpiData> dataList = capacityKpiDataRepository
					.findBySprintID(StringEscapeUtils.escapeSql(capacityMaster.getSprintNodeId()));
			CapacityKpiData capacityData;
			if (CollectionUtils.isNotEmpty(dataList)) {
				capacityData = dataList.get(0);
				capacityData.setBasicProjectConfigId(capacityMaster.getBasicProjectConfigId());
				createScrumAssigneeData(capacityData, capacityMaster);
			} else {
				capacityData = createCapacityData(capacityMaster);
			}
			capacityKpiDataRepository.save(capacityData);
			//remove this call after cache changes are updated in CapacityService class
			clearCache(CommonConstant.JIRA_KPI_CACHE);
			clearKpiCache(capacityMaster.getBasicProjectConfigId().toString(),
					KPICode.SPRINT_CAPACITY_UTILIZATION.getKpiId());
			processed = true;
		}
		return processed;
	}

	/**
	 * This method check for mandatory values for capacity data
	 *
	 * @param capacityMaster
	 *            contains data for validation
	 * @return boolean value
	 */
	private boolean capacityIdCheck(CapacityMaster capacityMaster) {
		boolean isValid;
		if (capacityMaster.isKanban()) {
			isValid = StringUtils.isNotEmpty(capacityMaster.getProjectNodeId())
					&& StringUtils.isNotEmpty(capacityMaster.getStartDate());
		} else {
			isValid = StringUtils.isNotEmpty(capacityMaster.getProjectNodeId())
					&& StringUtils.isNotEmpty(capacityMaster.getSprintNodeId());
		}
		return isValid;
	}

	/**
	 * creates scrum capacity object
	 *
	 * @param capacityMaster
	 *            contains data to be saved
	 * @return CapacityKpiData object
	 */
	private CapacityKpiData createCapacityData(CapacityMaster capacityMaster) {
		CapacityKpiData data = new CapacityKpiData();
		data.setProjectId(capacityMaster.getProjectNodeId());
		data.setProjectName(capacityMaster.getProjectName());
		data.setSprintID(capacityMaster.getSprintNodeId());
		data.setBasicProjectConfigId(capacityMaster.getBasicProjectConfigId());
		createScrumAssigneeData(data, capacityMaster);
		return data;
	}

	/**
	 * if assigneeDetails are present, then we will calculate all the available
	 * capacity and add it else in normal cases for the projects where assignee
	 * toggle is of then normally the capacity is added
	 *
	 * @param data
	 * @param capacityMaster
	 */
	private void createScrumAssigneeData(CapacityKpiData data, CapacityMaster capacityMaster) {
		if (capacityMaster.isAssigneeDetails() && CollectionUtils.isNotEmpty(capacityMaster.getAssigneeCapacity())) {
			List<AssigneeCapacity> assigneeList = capacityMaster.getAssigneeCapacity().stream()
					.filter(assigneeRole -> StringUtils.isNotEmpty(assigneeRole.getUserId())
							&& (StringUtils.isNotEmpty(assigneeRole.getUserName())))
					.collect(Collectors.toList());

			double sum = assigneeList.stream()
					.mapToDouble(assignee -> Optional.ofNullable(assignee.getAvailableCapacity()).orElse(0.0d)).sum();
			data.setAssigneeCapacity(assigneeList);
			data.setCapacityPerSprint(Math.round(sum * 100) / 100.0);
			// Set the LeafNodeCapacity list in data
			data.setAdditionalFilterCapacityList(saveAdditionalFilterCapacity(capacityMaster));

		} else {
			data.setCapacityPerSprint(capacityMaster.getCapacity());
			data.setAdditionalFilterCapacityList(saveAdditionalFilterCapacity(capacityMaster));
		}

	}

	private void createKanbanAssigneeData(KanbanCapacity data, CapacityMaster capacityMaster) {
		if (capacityMaster.isAssigneeDetails() && CollectionUtils.isNotEmpty(capacityMaster.getAssigneeCapacity())) {
			List<AssigneeCapacity> assigneeList = capacityMaster.getAssigneeCapacity().stream()
					.filter(assigneeRole -> StringUtils.isNotEmpty(assigneeRole.getUserId())
							&& (StringUtils.isNotEmpty(assigneeRole.getUserName())))
					.collect(Collectors.toList());
			double sum = assigneeList.stream()
					.mapToDouble(assignee -> Optional.ofNullable(assignee.getAvailableCapacity()).orElse(0.0d)).sum();
			data.setAssigneeCapacity(assigneeList);
			helper(capacityMaster);
			data.setAdditionalFilterCapacityList(saveAdditionalFilterCapacity(capacityMaster));

			// we have to divide capacity by working days of week
			data.setCapacity(sum / 5);
		} else {
			data.setCapacity(capacityMaster.getCapacity() / 5);
			// dividing each leaf node capacity of full week to single day.
			helper(capacityMaster);
			data.setAdditionalFilterCapacityList(saveAdditionalFilterCapacity(capacityMaster));

		}

	}

	/**
	 * creates kanban team capacity object
	 *
	 * @param capacityMaster
	 *            contains data to be saved
	 * @return KanbanCapacity object
	 */
	private KanbanCapacity createKanbanCapacityData(CapacityMaster capacityMaster) {
		KanbanCapacity data = new KanbanCapacity();
		data.setProjectId(capacityMaster.getProjectNodeId());
		data.setProjectName(capacityMaster.getProjectName());
		data.setBasicProjectConfigId(capacityMaster.getBasicProjectConfigId());
		createKanbanAssigneeData(data, capacityMaster);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
		data.setStartDate(LocalDate.parse(capacityMaster.getStartDate(), dateTimeFormatter));
		data.setEndDate(LocalDate.parse(capacityMaster.getEndDate(), dateTimeFormatter));
		return data;
	}

	/**
	 * method to get week start and end date
	 *
	 * @param date
	 *            any date in week
	 * @param isWeekend
	 *            flag to decide week start or end date to pass
	 * @return date according to flag
	 */
	private String weekDate(String date, boolean isWeekend) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(DateUtil.dateTimeParser(date, DATE_FORMAT));
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		if (isWeekend) {
			cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() + 6);
		} else {
			cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		}

		return DateUtil.dateTimeFormatter(cal.getTime(), DATE_FORMAT);
	}

	/**
	 * method to create filtermap
	 *
	 * @param capacityMaster
	 *            capacityMaster
	 * @return filterMap
	 */
	private Map<String, String> kanbanFilterMap(CapacityMaster capacityMaster) {
		Map<String, String> filter = Maps.newHashMap();
		filter.put("projectId", capacityMaster.getProjectNodeId());
		return filter;
	}

	/**
	 * Clears filter and jira related cache.
	 *
	 * @param cacheName
	 *            cacheName
	 */
	private void clearCache(final String cacheName) {
		cacheService.clearCache(cacheName);
	}

	/**
	 * Clears kpi cache for given project and kpi.
	 * 
	 * @param basicProjectConfigId
	 *            project basic config id
	 * @param kpiId
	 *            kpi id
	 */
	private void clearKpiCache(String basicProjectConfigId, String kpiId) {
		kpiDataCacheService.clearCache(basicProjectConfigId, kpiId);
	}

	/**
	 * delete capacity by basicProjectConfigId
	 *
	 * @param isKanban
	 *            isKanban
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	public void deleteCapacityByProject(boolean isKanban, ObjectId basicProjectConfigId) {
		if (isKanban) {
			kanbanCapacityRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
		} else {
			capacityKpiDataRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
		}
	}

	/**
	 * Helper method to process the additional filter capacity list in the given
	 * CapacityMaster. If a filter ID and its node capacities are valid, it divides
	 * each node capacity by 5.
	 *
	 * @param capacityMaster
	 *            the CapacityMaster object containing additional filter capacities
	 */
	public void helper(CapacityMaster capacityMaster) {
		// Retrieve the additional filter hierarchy levels and convert keys to uppercase
		// for case-insensitive matching
		Map<String, AdditionalFilterCategory> addFilterCat = filterHelperService.getAdditionalFilterHierarchyLevel();
		Map<String, AdditionalFilterCategory> addFilterCategory = addFilterCat.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Map.Entry::getValue));

		// Check if the additional filter capacity list in CapacityMaster is not empty
		if (CollectionUtils.isNotEmpty(capacityMaster.getAdditionalFilterCapacityList())) {
			// Iterate through each additional filter capacity
			for (AdditionalFilterCapacity category : capacityMaster.getAdditionalFilterCapacityList()) {
				// Check if the filter ID is not empty, node capacity list is not empty, and the
				// filter ID exists in additional filter categories
				if (StringUtils.isNotEmpty(category.getFilterId())
						&& CollectionUtils.isNotEmpty(category.getNodeCapacityList())
						&& addFilterCategory.get(category.getFilterId().toUpperCase()) != null) {

					// For each node capacity, divide the additional filter capacity by 5 if it's
					// not null
					category.getNodeCapacityList().forEach(leafNode -> {
						Double leafNodeCapacity = leafNode.getAdditionalFilterCapacity();
						if (leafNodeCapacity != null) {
							leafNode.setAdditionalFilterCapacity(leafNodeCapacity / 5);
						}
					});
				}
			}
		}
	}

	/**
	 * Saves the additional filter capacities from the given capacity master.
	 *
	 * @param capacityMaster
	 *            the capacity master containing additional filter capacities
	 * @return a list of saved additional filter capacities
	 */
	public List<AdditionalFilterCapacity> saveAdditionalFilterCapacity(CapacityMaster capacityMaster) {
		if (CollectionUtils.isEmpty(capacityMaster.getAdditionalFilterCapacityList())) {
			return new ArrayList<>();
		}

		return capacityMaster.getAdditionalFilterCapacityList().stream().filter(this::isValidAdditionalFilterCapacity)
				.map(this::createDbAdditionalFilterCapacity).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * Validates if the given additional filter capacity has a non-empty filter ID.
	 *
	 * @param additionalFilterCapacity
	 *            the additional filter capacity to validate
	 * @return true if the filter ID is not empty, false otherwise
	 */
	private boolean isValidAdditionalFilterCapacity(AdditionalFilterCapacity additionalFilterCapacity) {
		return StringUtils.isNotEmpty(additionalFilterCapacity.getFilterId());
	}

	/**
	 * Creates a database entity for the given additional filter capacity.
	 *
	 * @param additionalFilterCapacity
	 *            the additional filter capacity to convert
	 * @return the corresponding database entity, or null if no valid node
	 *         capacities exist
	 */
	private AdditionalFilterCapacity createDbAdditionalFilterCapacity(
			AdditionalFilterCapacity additionalFilterCapacity) {
		List<LeafNodeCapacity> dbNodeCapacityList = additionalFilterCapacity.getNodeCapacityList().stream()
				.filter(this::isValidNodeCapacity).map(this::createDbLeafNodeCapacity).collect(Collectors.toList());

		if (CollectionUtils.isEmpty(dbNodeCapacityList)) {
			return null;
		}

		AdditionalFilterCapacity dbFilterCapacity = new AdditionalFilterCapacity();
		dbFilterCapacity.setFilterId(additionalFilterCapacity.getFilterId());
		dbFilterCapacity.setNodeCapacityList(dbNodeCapacityList);
		return dbFilterCapacity;
	}

	/**
	 * Validates if the given node capacity has a non-empty additional filter ID and
	 * a non-null additional filter capacity.
	 *
	 * @param nodeCapacity
	 *            the node capacity to validate
	 * @return true if the additional filter ID is not empty and the additional
	 *         filter capacity is not null, false otherwise
	 */
	private boolean isValidNodeCapacity(LeafNodeCapacity nodeCapacity) {
		return StringUtils.isNotEmpty(nodeCapacity.getAdditionalFilterId())
				&& ObjectUtils.isNotEmpty(nodeCapacity.getAdditionalFilterCapacity());
	}

	/**
	 * Creates a database entity for the given node capacity.
	 *
	 * @param nodeCapacity
	 *            the node capacity to convert
	 * @return the corresponding database entity
	 */
	private LeafNodeCapacity createDbLeafNodeCapacity(LeafNodeCapacity nodeCapacity) {
		return new LeafNodeCapacity(nodeCapacity.getAdditionalFilterId(), nodeCapacity.getAdditionalFilterCapacity());
	}
}
