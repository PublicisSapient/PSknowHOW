package com.publicissapient.kpidashboard.apis.capacity.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.jira.service.SprintDetailsService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AssigneeCapacity;
import com.publicissapient.kpidashboard.common.model.application.CapacityMaster;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Week;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.excel.KanbanCapacityRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * @author narsingh9
 *
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
	private ProjectBasicConfigService projectBasicConfigService;

	@Autowired
	private SprintDetailsService sprintDetailsService;

	@Autowired
	private CustomApiConfig customApiConfig;

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

		for (SprintDetails sprint : sprintDetails) {
			CapacityKpiData capacityKpiData = capacityKpiDataList.stream()
					.filter(capacityData -> capacityData.getSprintID().equals(sprint.getSprintID())).findAny()
					.orElse(null);

			CapacityMaster capacityMaster = new CapacityMaster();

			if (capacityKpiData != null) {
				capacityMaster.setId(capacityKpiData.getId());
				capacityMaster.setCapacity(capacityKpiData.getCapacityPerSprint());
			} else {
				capacityMaster.setId(null);
				capacityMaster.setCapacity(0D);
			}
			capacityMaster.setSprintName(sprint.getSprintName());
			capacityMaster.setSprintState(sprint.getState());
			capacityMaster.setSprintNodeId(sprint.getSprintID());

			setProjectMeta(capacityMaster, project);

			capacityMasterList.add(capacityMaster);

		}

		return capacityMasterList;
	}

	private List<SprintDetails> filterSprints(List<SprintDetails> allSprints) {
		List<SprintDetails> sprints = new ArrayList<>();

		List<SprintDetails> closedSprints = allSprints.stream()
				.filter(sprintDetails -> SprintDetails.SPRINT_STATE_CLOSED.equalsIgnoreCase(sprintDetails.getState()))
				.sorted(Comparator.comparing((SprintDetails sprintDetails) -> LocalDateTime.parse(sprintDetails.getStartDate(),
						DateTimeFormatter.ofPattern(SPRINT_DATE_FORMAT))).reversed())
				.limit(customApiConfig.getSprintCountForFilters()).collect(Collectors.toList());

		List<SprintDetails> activeSprints = allSprints.stream()
				.filter(sprintDetails -> SprintDetails.SPRINT_STATE_ACTIVE.equalsIgnoreCase(sprintDetails.getState()))
				.collect(Collectors.toList());

		List<SprintDetails> futureSprints = allSprints.stream()
				.filter(sprintDetails -> SprintDetails.SPRINT_STATE_FUTURE.equalsIgnoreCase(sprintDetails.getState()))
				.collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(futureSprints)) {
			sprints.addAll(futureSprints);
		}
		if (CollectionUtils.isNotEmpty(activeSprints)) {
			sprints.addAll(activeSprints);
		}
		if (CollectionUtils.isNotEmpty(closedSprints)) {
			sprints.addAll(closedSprints);
		}

		return sprints;
	}

	private List<CapacityMaster> getCapacityDataForKanban(ProjectBasicConfig project) {
		List<CapacityMaster> capacityMasterList = new ArrayList<>();
		List<KanbanCapacity> kanbanCapacitiesAll = kanbanCapacityRepository.findByBasicProjectConfigId(project.getId());
		List<Week> weeksToShow = getWeeksToShow();

		for (Week week : weeksToShow) {
			CapacityMaster capacityMasterKanban = new CapacityMaster();

			if (CollectionUtils.isNotEmpty(kanbanCapacitiesAll)) {
				KanbanCapacity kanbanCapacity = kanbanCapacitiesAll.stream()
						.filter(capacity -> week.getStartDate().equals(capacity.getStartDate())).findAny().orElse(null);
				if (kanbanCapacity != null) {
					capacityMasterKanban.setId(kanbanCapacity.getId());
					capacityMasterKanban.setCapacity(kanbanCapacity.getCapacity());
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
					capacityMasterKanban.setStartDate(kanbanCapacity.getStartDate().format(formatter));
					capacityMasterKanban.setEndDate(kanbanCapacity.getEndDate().format(formatter));
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

			setProjectMeta(capacityMasterKanban, project);

			capacityMasterList.add(capacityMasterKanban);

		}
		return capacityMasterList;
	}

	private void setProjectMeta(CapacityMaster capacityMaster, ProjectBasicConfig project) {
		capacityMaster.setProjectNodeId(project.getProjectName() + "_" + project.getId().toHexString());
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
				createKanbanAssigneeData(capacityData,capacityMaster);
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
					.findBySprintID( StringEscapeUtils.escapeSql(capacityMaster.getSprintNodeId()));
			CapacityKpiData capacityData;
			if (CollectionUtils.isNotEmpty(dataList)) {
				capacityData = dataList.get(0);
				capacityData.setBasicProjectConfigId(capacityMaster.getBasicProjectConfigId());
				createScrumAssigneeData(capacityData,capacityMaster);
			} else {
				capacityData = createCapacityData(capacityMaster);
			}
			capacityKpiDataRepository.save(capacityData);
			clearCache(CommonConstant.JIRA_KPI_CACHE);
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
			isValid =  StringUtils.isNotEmpty(capacityMaster.getProjectNodeId())
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

	private void createScrumAssigneeData(CapacityKpiData data, CapacityMaster capacityMaster) {
		if (capacityMaster.isAssigneeDetails() && CollectionUtils.isNotEmpty(capacityMaster.getAssigneeCapacity())) {
			List<AssigneeCapacity> assigneeList = capacityMaster.getAssigneeCapacity().stream()
					.filter(assigneeRole -> StringUtils.isNotEmpty(assigneeRole.getUserId())
							&& (StringUtils.isNotEmpty(assigneeRole.getUserName())))
					.collect(Collectors.toList());

			double sum = assigneeList.stream()
					.mapToDouble(assignee -> Optional.ofNullable(assignee.getAvailableCapacity()).orElse(0.0d)).sum();
			data.setAssigneeCapacity(assigneeList);
			data.setCapacityPerSprint(sum);
		} else {
			data.setCapacityPerSprint(capacityMaster.getCapacity());
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
			data.setCapacity(sum);
		} else {
			data.setCapacity(capacityMaster.getCapacity());
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
		createKanbanAssigneeData(data,capacityMaster);
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
	 * delete capacity by basicProjectConfigId
	 * 
	 * @param isKanban
	 *            isKanban
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	public void deleteCapacityByProject(boolean isKanban, ObjectId basicProjectConfigId) {
		if(isKanban) {
			kanbanCapacityRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
		}else {
			capacityKpiDataRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
		}
	}
	
}
