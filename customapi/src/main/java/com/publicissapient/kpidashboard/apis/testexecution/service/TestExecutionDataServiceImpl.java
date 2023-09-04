package com.publicissapient.kpidashboard.apis.testexecution.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.jira.service.SprintDetailsService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecutionData;
import com.publicissapient.kpidashboard.common.repository.application.KanbanTestExecutionRepository;
import com.publicissapient.kpidashboard.common.repository.application.TestExecutionRepository;

/**
 * @author sansharm13
 *
 */
@Service
public class TestExecutionDataServiceImpl implements TestExecutionService {

	private static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String SPRINT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	@Autowired
	private TestExecutionRepository testExecutionRepository;
	@Autowired
	private KanbanTestExecutionRepository kanbanTestExecutionRepo;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private ProjectBasicConfigService projectBasicConfigService;
	@Autowired
	private SprintDetailsService sprintDetailsService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * This method process the test Execution data.
	 * 
	 * @param testExecutionData
	 *            testExecution
	 * @return TestExecution object
	 */
	public TestExecutionData processTestExecutionData(TestExecutionData testExecutionData) {
		boolean saved = false;
		if (testExecutionData.isKanban()) {
			saved = processKanbanTestExecutionData(testExecutionData);
		} else {
			saved = processScrumTestExecutionData(testExecutionData);
		}
		if (!saved) {
			testExecutionData = null;
		}
		return testExecutionData;
	}

	@Override
	public List<TestExecutionData> getTestExecutions(String basicProjectConfigId) {
		ProjectBasicConfig project = projectBasicConfigService.getProjectBasicConfigs(basicProjectConfigId);
		List<TestExecutionData> testExecutions = new ArrayList<>();
		if (project != null) {
			if (project.getIsKanban()) {
				testExecutions.addAll(getTestExecutionsForKanban(project));
			} else {
				testExecutions.addAll(getTestExecutionsForScrum(project));
			}
		}
		return testExecutions;
	}

	private Collection<TestExecutionData> getTestExecutionsForScrum(ProjectBasicConfig project) {
		List<TestExecutionData> testExecutions = new ArrayList<>();
		List<SprintDetails> sprintDetails = filterSprints(
				sprintDetailsService.getSprintDetails(project.getId().toHexString()));
		FieldMapping fieldMapping = configHelperService.getFieldMapping(project.getId());
		List<String> sprintIds = sprintDetails.stream().map(SprintDetails::getSprintID).collect(Collectors.toList());
		List<TestExecution> savedTestExecutions = testExecutionRepository.findBySprintIdIn(sprintIds);

		for (SprintDetails sprint : sprintDetails) {
			TestExecution savedTestExecution = savedTestExecutions.stream()
					.filter(capacityData -> capacityData.getSprintId().equals(sprint.getSprintID())).findAny()
					.orElse(null);
			TestExecutionData testExecutionData = new TestExecutionData();

			boolean isUploadEnable = fieldMapping.isUploadDataKPI42() || fieldMapping.isUploadDataKPI16();
			if (savedTestExecution != null) {
				testExecutionData.setExecutedTestCase(savedTestExecution.getExecutedTestCase());
				testExecutionData.setPassedTestCase(savedTestExecution.getPassedTestCase());
				testExecutionData.setTotalTestCases(savedTestExecution.getTotalTestCases());
				testExecutionData.setAutomatedTestCases(savedTestExecution.getAutomatedTestCases());
				testExecutionData.setAutomatableTestCases(savedTestExecution.getAutomatableTestCases());
				testExecutionData.setAutomatedRegressionTestCases(savedTestExecution.getAutomatedRegressionTestCases());
				testExecutionData.setTotalRegressionTestCases(savedTestExecution.getTotalRegressionTestCases());
				testExecutionData.setUploadEnable(isUploadEnable);

			} else {
				testExecutionData.setExecutedTestCase(0);
				testExecutionData.setPassedTestCase(0);
				testExecutionData.setTotalTestCases(0);
				testExecutionData.setAutomatedTestCases(0);
				testExecutionData.setAutomatableTestCases(0);
				testExecutionData.setAutomatedRegressionTestCases(0);
				testExecutionData.setTotalRegressionTestCases(0);
				testExecutionData.setUploadEnable(isUploadEnable);
			}

			testExecutionData.setSprintName(sprint.getSprintName());
			testExecutionData.setSprintState(sprint.getState());
			testExecutionData.setSprintId(sprint.getSprintID());

			setProjectMeta(testExecutionData, project);

			testExecutions.add(testExecutionData);

		}
		return testExecutions;
	}

	private List<SprintDetails> filterSprints(List<SprintDetails> allSprints) {
		List<SprintDetails> sprints = new ArrayList<>();

		List<SprintDetails> closedSprints = allSprints.stream()
				.filter(sprintDetails -> SprintDetails.SPRINT_STATE_CLOSED.equalsIgnoreCase(sprintDetails.getState()))
				.sorted(Comparator.comparing((SprintDetails sprintDetails) -> LocalDateTime
						.parse(sprintDetails.getStartDate(), DateTimeFormatter.ofPattern(SPRINT_DATE_FORMAT)))
						.reversed())
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

	private Collection<TestExecutionData> getTestExecutionsForKanban(ProjectBasicConfig project) {
		List<TestExecutionData> testExecutions = new ArrayList<>();

		List<KanbanTestExecution> testExecutionsAll = kanbanTestExecutionRepo
				.findByBasicProjectConfigId(project.getId().toHexString());

		List<LocalDate> datesToShow = datesToShowForKanban();

		for (LocalDate executionDate : datesToShow) {
			TestExecutionData testExecutionData = new TestExecutionData();
			if (CollectionUtils.isNotEmpty(testExecutionsAll)) {
				KanbanTestExecution savedTestExecution = testExecutionsAll.stream()
						.filter(testExecution -> LocalDate
								.parse(testExecution.getExecutionDate(), DateTimeFormatter.ofPattern(DATE_FORMAT))
								.equals(executionDate))
						.findAny().orElse(null);
				if (savedTestExecution != null) {
					testExecutionData.setExecutedTestCase(savedTestExecution.getExecutedTestCase());
					testExecutionData.setPassedTestCase(savedTestExecution.getPassedTestCase());
					testExecutionData.setTotalTestCases(savedTestExecution.getTotalTestCases());
				} else {
					testExecutionData.setExecutedTestCase(0);
					testExecutionData.setPassedTestCase(0);
					testExecutionData.setTotalTestCases(0);
				}

			} else {
				testExecutionData.setExecutedTestCase(0);
				testExecutionData.setPassedTestCase(0);
				testExecutionData.setTotalTestCases(0);
			}

			testExecutionData.setExecutionDate(executionDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));

			setProjectMeta(testExecutionData, project);

			testExecutions.add(testExecutionData);

		}

		return testExecutions;
	}

	private void setProjectMeta(TestExecutionData testExecutionData, ProjectBasicConfig project) {
		if (testExecutionData != null && project != null) {
			testExecutionData.setProjectNodeId(project.getProjectName() + "_" + project.getId().toHexString());
			testExecutionData.setProjectName(project.getProjectName());
			testExecutionData.setBasicProjectConfigId(project.getId().toHexString());
			testExecutionData.setKanban(project.getIsKanban());
		}

	}

	private List<LocalDate> datesToShowForKanban() {
		List<LocalDate> dates = new ArrayList<>();
		int pastDays = customApiConfig.getNumberOfPastDaysForKanbanTestExecution();
		int futureDays = customApiConfig.getNumberOfFutureDaysForKanbanTestExecution();
		LocalDate currentDate = LocalDate.now();

		for (int i = 1; i <= futureDays; i++) {

			dates.add(currentDate.plusDays(i));
		}
		dates.add(currentDate);

		for (int i = 1; i <= pastDays; i++) {

			dates.add(currentDate.minusDays(i));
		}

		return dates.stream().sorted().collect(Collectors.toList());
	}

	/**
	 * process sprint TestExecution data
	 * 
	 * @param testExecutionData
	 *            contains data to be saved
	 * @return boolean value
	 */
	private boolean processScrumTestExecutionData(TestExecutionData testExecutionData) {
		boolean processed = false;
		if (testExecutionIdNullCheck(testExecutionData)) {
			TestExecution existingTestExecutionData = testExecutionRepository
					.findBySprintId(testExecutionData.getSprintId());
			TestExecution testExecution = null;
			if (existingTestExecutionData != null) {
				testExecution = existingTestExecutionData;
				testExecution.setTotalTestCases(ObjectUtils.defaultIfNull(testExecutionData.getTotalTestCases(),
						existingTestExecutionData.getTotalTestCases()));
				testExecution.setExecutedTestCase(ObjectUtils.defaultIfNull(testExecutionData.getExecutedTestCase(),
						existingTestExecutionData.getExecutedTestCase()));
				testExecution.setPassedTestCase(ObjectUtils.defaultIfNull(testExecutionData.getPassedTestCase(),
						existingTestExecutionData.getPassedTestCase()));
				testExecution.setAutomatedTestCases(ObjectUtils.defaultIfNull(testExecutionData.getAutomatedTestCases(),
						existingTestExecutionData.getAutomatedTestCases()));
				testExecution
						.setAutomatableTestCases(ObjectUtils.defaultIfNull(testExecutionData.getAutomatableTestCases(),
								existingTestExecutionData.getAutomatableTestCases()));
				testExecution.setAutomatedRegressionTestCases(
						ObjectUtils.defaultIfNull(testExecutionData.getAutomatedRegressionTestCases(),
								existingTestExecutionData.getAutomatedRegressionTestCases()));
				testExecution.setTotalRegressionTestCases(
						ObjectUtils.defaultIfNull(testExecutionData.getTotalRegressionTestCases(),
								existingTestExecutionData.getTotalRegressionTestCases()));
			} else {
				testExecution = createScrumTestExecutionData(testExecutionData);
			}
			testExecutionRepository.save(testExecution);
			clearCache(CommonConstant.TESTING_KPI_CACHE);
			processed = true;
		}
		return processed;
	}

	/**
	 * This method check for mandatory values for test Execution data
	 * 
	 * @param testExecution
	 *            contains data for validation
	 * @return boolean value
	 */
	private boolean testExecutionIdNullCheck(TestExecutionData testExecution) {
		boolean isValid = false;
		if (testExecution.isKanban()) {
			isValid = StringUtils.isNotEmpty(testExecution.getProjectNodeId());
		} else {
			isValid = StringUtils.isNotEmpty(testExecution.getProjectNodeId())
					&& StringUtils.isNotEmpty(testExecution.getSprintId());
		}
		return isValid;
	}

	/**
	 * process kanban testExecutionData data
	 * 
	 * @param testExecutionData
	 *            contains data to be saved
	 * @return boolean value
	 */
	private boolean processKanbanTestExecutionData(TestExecutionData testExecutionData) {
		boolean processed = false;
		if (!checkDateFormate(testExecutionData.getExecutionDate())) {
			throw new IllegalArgumentException("Date Formate should be in yy-mm-dd");
		}
		if (testExecutionIdNullCheck(testExecutionData)) {
			KanbanTestExecution testExecutiondata = kanbanTestExecutionRepo.findByExecutionDateAndProjectNodeId(
					testExecutionData.getExecutionDate(), testExecutionData.getProjectNodeId());
			KanbanTestExecution kanbanTestExecutionData = null;
			if (testExecutiondata != null) {
				kanbanTestExecutionData = testExecutiondata;
				kanbanTestExecutionData.setExecutedTestCase(testExecutionData.getExecutedTestCase());
				kanbanTestExecutionData.setTotalTestCases(testExecutionData.getTotalTestCases());
				kanbanTestExecutionData.setExecutionDate(testExecutionData.getExecutionDate());
				kanbanTestExecutionData.setPassedTestCase(testExecutionData.getPassedTestCase());
			} else {
				kanbanTestExecutionData = createKanbanTestExecutionData(testExecutionData);
			}

			kanbanTestExecutionRepo.save(kanbanTestExecutionData);
			cacheService.clearAllCache();
			processed = true;
		}
		return processed;
	}

	private TestExecution createScrumTestExecutionData(TestExecutionData testExecutionData) {
		TestExecution testExecution = new TestExecution();
		testExecution.setProjectId(testExecutionData.getProjectNodeId());
		testExecution.setProjectName(testExecutionData.getProjectName());
		testExecution.setSprintId(testExecutionData.getSprintId());
		testExecution.setSprintName(testExecutionData.getSprintName());
		testExecution.setTotalTestCases(testExecutionData.getTotalTestCases());
		testExecution.setExecutedTestCase(testExecutionData.getExecutedTestCase());
		testExecution.setPassedTestCase(testExecutionData.getPassedTestCase());
		testExecution.setAutomatedTestCases(testExecutionData.getAutomatedTestCases());
		testExecution.setAutomatableTestCases(testExecutionData.getAutomatableTestCases());
		testExecution.setAutomatedRegressionTestCases(testExecutionData.getAutomatedRegressionTestCases());
		testExecution.setTotalRegressionTestCases(testExecutionData.getTotalRegressionTestCases());
		testExecution.setBasicProjectConfigId(testExecutionData.getBasicProjectConfigId());
		return testExecution;
	}

	private KanbanTestExecution createKanbanTestExecutionData(TestExecutionData testExecutionData) {

		KanbanTestExecution kanbanTestExecution = new KanbanTestExecution();
		kanbanTestExecution.setProjectName(testExecutionData.getProjectName());
		kanbanTestExecution.setProjectNodeId(testExecutionData.getProjectNodeId());
		kanbanTestExecution.setExecutionDate(testExecutionData.getExecutionDate());
		kanbanTestExecution.setTotalTestCases(testExecutionData.getTotalTestCases());
		kanbanTestExecution.setExecutedTestCase(testExecutionData.getExecutedTestCase());
		kanbanTestExecution.setPassedTestCase(testExecutionData.getPassedTestCase());
		kanbanTestExecution.setBasicProjectConfigId(testExecutionData.getBasicProjectConfigId());
		return kanbanTestExecution;
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
	 * process kanban DATE testExecutionData
	 * 
	 * @param value
	 *            DATE VALUE
	 * @return boolean date
	 */
	public boolean checkDateFormate(String value) {
		boolean date = false;
		date = value.matches(DATE_PATTERN);
		if (date) {
			return true;
		}
		return date;

	}

	/**
	 * delete test execution by basicProjectConfigId
	 * 
	 * @param isKanban
	 *            isKanban
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	public void deleteTestExecutionByProject(boolean isKanban, String basicProjectConfigId) {
		if (isKanban) {
			kanbanTestExecutionRepo.deleteByBasicProjectConfigId(basicProjectConfigId);
		} else {
			testExecutionRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
		}
	}
}
