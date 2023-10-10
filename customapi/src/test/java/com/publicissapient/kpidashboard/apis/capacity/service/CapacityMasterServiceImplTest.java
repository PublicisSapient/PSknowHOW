package com.publicissapient.kpidashboard.apis.capacity.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.collections.Lists;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.CapacityKpiDataDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanCapacityDataFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.jira.service.SprintDetailsService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.model.application.AssigneeCapacity;
import com.publicissapient.kpidashboard.common.model.application.CapacityMaster;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.excel.KanbanCapacityRepository;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;

/**
 * @author narsingh9
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CapacityMasterServiceImplTest {

	CapacityMaster scrumCapacityMaster;
	CapacityMaster scrumCapacityAssigneeMaster;
	CapacityMaster kanbanCapacity;
	CapacityMaster kanbanCapacityAssignee;
	KanbanCapacity kanbanDbData;
	List<KanbanCapacity> kanbanCapacityList;
	List<KanbanCapacity> kanbanCapacityAsigneeList;
	List<CapacityKpiData> capacityKpiDataList;
	List<CapacityKpiData> capacityAssigneeKpiDataList;
	private MockMvc mockMvc;
	@InjectMocks
	private CapacityMasterServiceImpl capacityMasterServiceImpl;
	@Mock
	private CapacityKpiDataRepository capacityKpiDataRepository;
	@Mock
	private KanbanCapacityRepository kanbanCapacityRepository;
	@Mock
	private CacheService cacheService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private ProjectBasicConfigService projectBasicConfigService;
	@Mock
	private SprintDetailsService sprintDetailsService;
	@Mock
	private HappinessKpiDataRepository happinessKpiDataRepository;
	private List<SprintDetails> sprintDetailsList;

	/**
	 * initialize values to be used in testing
	 */
	@Before
	public void setUp() {
		kanbanCapacityList = KanbanCapacityDataFactory.newInstance().getKanbanCapacityDataList();
		capacityKpiDataList = CapacityKpiDataDataFactory.newInstance().getCapacityKpiDataList();
		sprintDetailsList = SprintDetailsDataFactory.newInstance().getSprintDetails();
		setUpCapacityAssignee();
		mockMvc = MockMvcBuilders.standaloneSetup(capacityMasterServiceImpl).build();
		scrumCapacityMaster = new CapacityMaster();
		scrumCapacityMaster.setProjectName("Scrum Project");
		scrumCapacityMaster.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
		scrumCapacityMaster.setSprintNodeId("38296_Scrum Project_6335363749794a18e8a4479b");
		scrumCapacityMaster.setKanban(false);
		scrumCapacityMaster.setCapacity(500.0);

		kanbanCapacity = new CapacityMaster();
		kanbanCapacity.setProjectName("Kanban Project");
		kanbanCapacity.setProjectNodeId("Kanban Project_6335368249794a18e8a4479f");
		kanbanCapacity.setStartDate("2021-01-31");
		kanbanCapacity.setEndDate("2020-02-02");
		kanbanCapacity.setKanban(true);
		kanbanCapacity.setCapacity(500.0);

		kanbanDbData = new KanbanCapacity();
		kanbanDbData.setProjectName("health project");
		kanbanDbData.setProjectId("Kanban Project_6335368249794a18e8a4479f");
		kanbanDbData.setCapacity(200.0);
	}

	private void setUpCapacityAssignee() {
		kanbanCapacityAsigneeList = KanbanCapacityDataFactory.newInstance("/json/kanban/kanban_capacity_assignee.json")
				.getKanbanCapacityDataList();
		capacityAssigneeKpiDataList = CapacityKpiDataDataFactory
				.newInstance("/json/default/capacity_assignee_kpi_data.json").getCapacityKpiDataList();

		scrumCapacityAssigneeMaster = new CapacityMaster();
		scrumCapacityAssigneeMaster.setProjectName("Scrum Project");
		scrumCapacityAssigneeMaster.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
		scrumCapacityAssigneeMaster.setSprintNodeId("40203_Scrum Project_6335363749794a18e8a4479b");
		scrumCapacityAssigneeMaster.setKanban(false);
		scrumCapacityAssigneeMaster.setCapacity(500.0);
		scrumCapacityAssigneeMaster.setAssigneeDetails(true);
		List<AssigneeCapacity> assigneeCapacityList = new ArrayList<>();
		assigneeCapacityList.add(createAssigneeData("1234", "NewUser1", 56.0, 0.0));
		assigneeCapacityList.add(createAssigneeData("14", "NewUser2", 56.0, 18.0));
		scrumCapacityAssigneeMaster.setAssigneeCapacity(assigneeCapacityList);

		kanbanCapacityAssignee = new CapacityMaster();
		kanbanCapacityAssignee.setProjectName("Kanban Project");
		kanbanCapacityAssignee.setProjectNodeId("Kanban Project_6335368249794a18e8a4479f");
		kanbanCapacityAssignee.setStartDate("2021-01-31");
		kanbanCapacityAssignee.setEndDate("2020-02-02");
		kanbanCapacityAssignee.setKanban(true);
		kanbanCapacityAssignee.setCapacity(500.0);
		kanbanCapacityAssignee.setAssigneeDetails(true);
		kanbanCapacityAssignee.setAssigneeCapacity(assigneeCapacityList);

	}

	private AssigneeCapacity createAssigneeData(String userId, String userName, double planned, double leaves) {

		AssigneeCapacity assigneeCapacity = new AssigneeCapacity();
		assigneeCapacity.setUserId(userId);
		assigneeCapacity.setUserName(userName);
		assigneeCapacity.setPlannedCapacity(planned);
		assigneeCapacity.setLeaves(leaves);
		assigneeCapacity.setAvailableCapacity(planned - leaves);
		return assigneeCapacity;

	}

	/**
	 * scrum capacity data saving
	 */
	@Test
	public void testProcessCapacityData_scrum_success() {
		Map<String, String> map = new HashMap<>();
		map.put("projectId", scrumCapacityMaster.getProjectNodeId());
		assertNotNull(capacityMasterServiceImpl.processCapacityData(scrumCapacityMaster));
	}

	/**
	 * scrum capacity data not saving due to missing fields
	 */
	@Test
	public void testProcessCapacityData_scrum_failure() {
		Map<String, String> map = new HashMap<>();
		map.put("projectId", scrumCapacityMaster.getProjectNodeId());
		assertNotNull(capacityMasterServiceImpl.processCapacityData(kanbanCapacity));
	}

	/**
	 * scrum capacity data saving
	 */
	@Test
	public void testProcessCapacityData_kanban_success() {
		assertNotNull(capacityMasterServiceImpl.processCapacityData(kanbanCapacity));
	}

	/**
	 * scrum capacity data saving
	 */
	@Test
	public void testProcessCapacityData_kanban_alreadyExist_success() {
		when(kanbanCapacityRepository.findByFilterMapAndDate(Mockito.any(), Mockito.any()))
				.thenReturn(Lists.newArrayList(kanbanDbData));
		assertNotNull(capacityMasterServiceImpl.processCapacityData(kanbanCapacity));
	}

	/**
	 * scrum capacity data not saving due to missing fields
	 */
	@Test
	public void testProcessCapacityData_kanban_failure() {
		kanbanCapacity = new CapacityMaster();
		assertNull(capacityMasterServiceImpl.processCapacityData(kanbanCapacity));
	}

	@Test
	public void getCapacities_ScrumSuccess() {
		ProjectBasicConfig project = createScrumProject(false);
		List<CapacityMaster> capacities = capacityMasterServiceImpl.getCapacities("6335363749794a18e8a4479b");
		assertEquals(0, capacities.size());
	}

	@Test
	public void getCapacities_ScrumWithNoSavedData() {
		ProjectBasicConfig project = createScrumProject(false);
		when(customApiConfig.getSprintCountForFilters()).thenReturn(5);
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);

		when(sprintDetailsService.getSprintDetails(anyString())).thenReturn(sprintDetailsList);

		when(capacityKpiDataRepository.findBySprintIDIn(anyList())).thenReturn(new ArrayList<>());
		when(happinessKpiDataRepository.findBySprintIDIn(Mockito.any())).thenReturn(new ArrayList<>());

		List<CapacityMaster> capacities = capacityMasterServiceImpl.getCapacities("6335363749794a18e8a4479b");

		assertEquals(7, capacities.size());
	}

	@Test
	public void getCapacities_KanbanSuccess() {
		ProjectBasicConfig project = createKanbanProject(false);
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);
		when(kanbanCapacityRepository.findByBasicProjectConfigId(Mockito.any(ObjectId.class)))
				.thenReturn(kanbanCapacityList);
		when(customApiConfig.getNumberOfPastWeeksForKanbanCapacity()).thenReturn(2);
		when(customApiConfig.getNumberOfFutureWeeksForKanbanCapacity()).thenReturn(2);
		List<CapacityMaster> capacities = capacityMasterServiceImpl.getCapacities("6335368249794a18e8a4479f");

		assertEquals(5, capacities.size());
	}

	@Test
	public void getCapacities_KanbanWithNoDataSaved() {
		ProjectBasicConfig project = createKanbanProject(false);
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);
		when(kanbanCapacityRepository.findByBasicProjectConfigId(Mockito.any(ObjectId.class)))
				.thenReturn(new ArrayList<>());
		when(customApiConfig.getNumberOfPastWeeksForKanbanCapacity()).thenReturn(2);
		when(customApiConfig.getNumberOfFutureWeeksForKanbanCapacity()).thenReturn(2);
		List<CapacityMaster> capacities = capacityMasterServiceImpl.getCapacities("6335368249794a18e8a4479f");

		assertEquals(5, capacities.size());
	}

	private ProjectBasicConfig createScrumProject(boolean assigneeData) {

		ProjectBasicConfig project = new ProjectBasicConfig();
		project.setId(new ObjectId("6335363749794a18e8a4479b"));
		project.setSaveAssigneeDetails(assigneeData);
		project.setIsKanban(false);
		return project;
	}

	private ProjectBasicConfig createKanbanProject(boolean assigneeDetails) {
		ProjectBasicConfig project = new ProjectBasicConfig();
		project.setId(new ObjectId("6335368249794a18e8a4479f"));
		project.setSaveAssigneeDetails(assigneeDetails);

		project.setIsKanban(true);
		return project;
	}

	@Test
	public void getCapacitiesAssigness_Scrum() {
		ProjectBasicConfig project = createScrumProject(true);
		when(customApiConfig.getSprintCountForFilters()).thenReturn(5);
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);
		when(sprintDetailsService.getSprintDetails(anyString())).thenReturn(sprintDetailsList);
		List<CapacityKpiData> collect = new ArrayList<>();
		for (SprintDetails sprintDetails : sprintDetailsList) {
			collect.addAll(capacityAssigneeKpiDataList.stream().filter(
					capacityKpiData -> capacityKpiData.getSprintID().equalsIgnoreCase(sprintDetails.getSprintID()))
					.collect(Collectors.toList()));
		}
		when(happinessKpiDataRepository.findBySprintIDIn(Mockito.any())).thenReturn(new ArrayList<>());
		when(capacityKpiDataRepository.findBySprintIDIn(anyList())).thenReturn(collect);
		List<CapacityMaster> capacities = capacityMasterServiceImpl.getCapacities("6335363749794a18e8a4479b");
		assertEquals(4,
				capacities.stream()
						.filter(capacityMaster -> CollectionUtils.isNotEmpty(capacityMaster.getAssigneeCapacity()))
						.collect(Collectors.toList()).size());
	}

	@Test
	public void getCapacitiesAssignees_Kanban() {
		updateKanbancapacity();
		ProjectBasicConfig project = createKanbanProject(true);
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);
		when(kanbanCapacityRepository.findByBasicProjectConfigId(Mockito.any(ObjectId.class)))
				.thenReturn(kanbanCapacityAsigneeList);
		when(customApiConfig.getNumberOfPastWeeksForKanbanCapacity()).thenReturn(2);
		when(customApiConfig.getNumberOfFutureWeeksForKanbanCapacity()).thenReturn(2);
		List<CapacityMaster> capacities = capacityMasterServiceImpl.getCapacities("6335368249794a18e8a4479f");
		assertEquals(3,
				capacities.stream()
						.filter(capacityMaster -> CollectionUtils.isNotEmpty(capacityMaster.getAssigneeCapacity()))
						.collect(Collectors.toList()).size());
	}

	private void updateKanbancapacity() {
		LocalDate monday = LocalDate.now();
		while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
			monday = monday.minusDays(1);
		}
		LocalDate sunday = LocalDate.now();
		while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
			sunday = sunday.plusDays(1);
		}

		LocalDate finalMonday = monday;
		LocalDate finalSunday = sunday;
		kanbanCapacityAsigneeList.stream().filter(kanbanCapacity1 -> {
			if (CollectionUtils.isNotEmpty(kanbanCapacity1.getAssigneeCapacity())
					&& kanbanCapacity1.getAssigneeCapacity().size() > 0) {
				return true;
			}
			return false;
		}).forEach(kanbanCapacity1 -> {
			kanbanCapacity1.setStartDate(finalMonday);
			kanbanCapacity1.setEndDate(finalSunday);
		});
	}

	@Test
	public void testProcessAssigneeCapacityData_scrum_success() {
		assertNotNull(capacityMasterServiceImpl.processCapacityData(scrumCapacityAssigneeMaster));
	}

	@Test
	public void testProcessAssigneeCapacityDataUpdate_success() {
		List<CapacityKpiData> collect = capacityAssigneeKpiDataList.stream().filter(capacityKpiData -> capacityKpiData
				.getSprintID().equalsIgnoreCase("38296_Scrum Project_6335363749794a18e8a4479b"))
				.collect(Collectors.toList());
		when(capacityKpiDataRepository.findBySprintID(anyString())).thenReturn(collect);
		assertNotNull(capacityMasterServiceImpl.processCapacityData(scrumCapacityAssigneeMaster));

	}

	@Test
	public void testProcessAssigneeCapacityData_kanban_success() {
		when(kanbanCapacityRepository.findByFilterMapAndDate(Mockito.any(), Mockito.any()))
				.thenReturn(Lists.newArrayList(kanbanDbData));
		assertNotNull(capacityMasterServiceImpl.processCapacityData(kanbanCapacityAssignee));
	}
}
