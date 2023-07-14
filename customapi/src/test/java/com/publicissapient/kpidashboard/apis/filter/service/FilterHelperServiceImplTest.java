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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.filter.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.Assert;

import com.google.gson.Gson;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchiesDataFactory;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchiesKanbanDataFactory;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.AdditionalFilterCategoryFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.dto.HierarchyLevelDTO;
import com.publicissapient.kpidashboard.common.model.application.dto.HierarchyValueDTO;
import com.publicissapient.kpidashboard.common.model.application.dto.ProjectBasicConfigDTO;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelSuggestionsServiceImpl;

/**
 * @author tauakram
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class FilterHelperServiceImplTest {

	Gson gson = new Gson();
	@Mock
	private CacheService cacheService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;
	@Mock
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;
	@Mock
	private HierarchyLevelSuggestionsServiceImpl hierarchyLevelSuggestionsService;
	@InjectMocks
	private FilterHelperService filterHelperService;
	@Mock
	private FilterHelperService filterHelperServiceMock;
	@Mock
	private HierarchyLevelService hierarchyLevelService;
	@Mock
	private HierarchyLevelRepository hierarchyLevelRepository;
	@Mock
	private AdditionalFilterCategoryRepository additionalFilterCategoryRepository;
	private List<AccountHierarchy> ahdList;
	private List<HierarchyLevel> hierarchyLevels;
	private List<HierarchyLevel> hierarchyLevels2;
	private ProjectBasicConfigDTO projectConfigScrum;
	private ProjectBasicConfigDTO projectConfigKanban;
	private KpiRequest kpiRequest;

	@Before
	public void setup() {
		AccountHierarchiesDataFactory ahdFactoryProjectLabel = AccountHierarchiesDataFactory.newInstance();
		ahdList = ahdFactoryProjectLabel.getAccountHierarchies();
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

		HierachyLevelFactory hierachyLevelFactory2 = HierachyLevelFactory.newInstance();
		hierarchyLevels2 = hierachyLevelFactory2.getHierarchyLevels();

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi55");
		setUpFilterCreation();
	}

	private void setUpFilterCreation() {
		List<HierarchyValueDTO> valueDTOList = new ArrayList<>();
		valueDTOList.add(setHierrachyValue(3, "hierarchyLevelThree", "Level Three", "Sample Three"));
		valueDTOList.add(setHierrachyValue(2, "hierarchyLevelTwo", "Level Two", "Sample Two"));
		valueDTOList.add(setHierrachyValue(1, "hierarchyLevelOne", "Level One", "Sample One"));

		projectConfigScrum = new ProjectBasicConfigDTO();
		projectConfigScrum.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfigScrum.setProjectName("Scrum Project");
		projectConfigScrum.setIsKanban(false);
		projectConfigScrum.setHierarchy(valueDTOList);

		List<HierarchyValueDTO> valueDTOList2 = new ArrayList<>();
		valueDTOList2.add(setHierrachyValue(3, "hierarchyLevelThree", "Level Three", "Sample Three"));
		valueDTOList2.add(setHierrachyValue(2, "hierarchyLevelTwo", "Level Two", "Sample Two"));
		valueDTOList2.add(setHierrachyValue(1, "hierarchyLevelOne", "Level One", "Sample One"));

		projectConfigKanban = new ProjectBasicConfigDTO();
		projectConfigKanban.setId(new ObjectId("6335368249794a18e8a4479f"));
		projectConfigKanban.setProjectName("Kanban Project");
		projectConfigKanban.setIsKanban(true);
		projectConfigKanban.setHierarchy(valueDTOList2);
	}

	private HierarchyValueDTO setHierrachyValue(int level, String levelId, String levelName, String value) {
		HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
		hierarchyLevelDTO.setLevel(level);
		hierarchyLevelDTO.setHierarchyLevelId(levelId);
		hierarchyLevelDTO.setHierarchyLevelName(levelName);
		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setValue(value);
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		return hierarchyValueDTO;
	}

	@Test
	public void getFilteredBuilds() {
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		List<AccountHierarchyData> accountHierarchyDataList = accountHierarchyFilterDataFactory
				.getAccountHierarchyDataList();
		when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);
		filterHelperService.getFilteredBuilds(kpiRequest, "sprint");
	}

	@Test
	public void getFilteredBuildsKanban() {
		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		List<AccountHierarchyDataKanban> accountHierarchyKanbanDataList = accountHierarchyKanbanFilterDataFactory
				.getAccountHierarchyKanbanDataList();
		when(cacheService.cacheAccountHierarchyKanbanData()).thenReturn(accountHierarchyKanbanDataList);

		try {
			filterHelperService.getFilteredBuildsKanban(
					KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json").findKpiRequest("kpi55"),
					"sqd");
		} catch (EntityNotFoundException e) {
			Assert.assertTrue(Boolean.TRUE);
		}

	}

	@Test
	public void filterCreationTest1() {
		when(accountHierarchyRepository.findAll()).thenReturn(ahdList);
		when(hierarchyLevelService.getFullHierarchyLevels(projectConfigScrum.isKanban())).thenReturn(hierarchyLevels);
		filterHelperService.filterCreation(projectConfigScrum);
		assertThat(accountHierarchyRepository.findAll().size(), equalTo(9));
	}

	@Test
	public void filterCreationTest2() {
		AccountHierarchiesKanbanDataFactory ahdFactoryProjectLabel = AccountHierarchiesKanbanDataFactory.newInstance();
		List<KanbanAccountHierarchy> accountHierarchies = ahdFactoryProjectLabel.getAccountHierarchies();
		when(kanbanAccountHierarchyRepo.findAll()).thenReturn(accountHierarchies);
		when(hierarchyLevelService.getFullHierarchyLevels(projectConfigKanban.isKanban())).thenReturn(hierarchyLevels);
		filterHelperService.filterCreation(projectConfigKanban);
		assertThat(kanbanAccountHierarchyRepo.findAll().size(), equalTo(4));
	}

	@Test
	public void cleanFilterData_Scrum() {
		when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId(Mockito.anyString(),
				Mockito.any(ObjectId.class)))
						.thenReturn(ahdList.stream().filter(f -> f.getLabelName().equalsIgnoreCase("project"))
								.collect(Collectors.toList()));
		when(accountHierarchyRepository.findByLabelNameAndPath(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(ahdList);
		doNothing().when(accountHierarchyRepository).deleteByPathEndsWith(Mockito.anyString());
		filterHelperService.cleanFilterData(projectConfigScrum);
		assertThat(ahdList.stream().filter(f -> f.getLabelName().equalsIgnoreCase("project"))
				.collect(Collectors.toList()).size(), equalTo(1));
	}

	@Test
	public void cleanFilterData_Kanban() {
		AccountHierarchiesKanbanDataFactory ahdFactoryProjectLabel = AccountHierarchiesKanbanDataFactory.newInstance();
		List<KanbanAccountHierarchy> accountHierarchiesProjetLabel = ahdFactoryProjectLabel.getAccountHierarchies();

		when(kanbanAccountHierarchyRepo.findByLabelNameAndBasicProjectConfigId(Mockito.anyString(),
				Mockito.any(ObjectId.class))).thenReturn(
						accountHierarchiesProjetLabel.stream().filter(f -> f.getLabelName().equalsIgnoreCase("project"))
								.collect(Collectors.toList()));

		when(kanbanAccountHierarchyRepo.findByLabelNameAndPath(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(accountHierarchiesProjetLabel);
		doNothing().when(kanbanAccountHierarchyRepo).deleteByNodeIdAndPath(Mockito.anyString(), Mockito.anyString());
		doNothing().when(kanbanAccountHierarchyRepo).deleteByPathEndsWith(Mockito.anyString());

		filterHelperService.cleanFilterData(projectConfigKanban);
	}

	@Test
	public void deleteAccountHierarchiesOfProject_Scrum() {
		ObjectId projectBasicConfigId = new ObjectId("60b9f2ff5ce907343c3804ba");
		when(hierarchyLevelService.getFullHierarchyLevels(projectConfigScrum.isKanban())).thenReturn(hierarchyLevels2);
		when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId(Mockito.anyString(),
				Mockito.any(ObjectId.class)))
						.thenReturn(ahdList.stream().filter(f -> f.getLabelName().equalsIgnoreCase("project"))
								.collect(Collectors.toList()));
		when(cacheService.getFullHierarchyLevelMap()).thenReturn(
				hierarchyLevels2.stream().collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x)));
		filterHelperService.deleteAccountHierarchiesOfProject(projectBasicConfigId, false);

	}

	@Test
	public void deleteAccountHierarchiesOfProject_Kanban() {
		ObjectId projectBasicConfigId = new ObjectId("63330b7068b5d05cf59c4386");
		AccountHierarchiesKanbanDataFactory ahdFactoryProjectLabel = AccountHierarchiesKanbanDataFactory.newInstance();
		List<KanbanAccountHierarchy> accountHierarchies = ahdFactoryProjectLabel.getAccountHierarchies();
		when(hierarchyLevelService.getFullHierarchyLevels(projectConfigKanban.isKanban())).thenReturn(hierarchyLevels2);

		when(cacheService.getFullKanbanHierarchyLevelMap()).thenReturn(
				hierarchyLevels.stream().collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x)));
		when(kanbanAccountHierarchyRepo.findByLabelNameAndBasicProjectConfigId(Mockito.anyString(),
				Mockito.any(ObjectId.class))).thenReturn(
						accountHierarchies.stream().filter(f -> f.getLabelName().equalsIgnoreCase("project"))
								.collect(Collectors.toList()));
		filterHelperService.deleteAccountHierarchiesOfProject(projectConfigKanban.getId(), true);

	}

	@Test
	public void getHierarachyLevelId() {
		when(cacheService.getFullHierarchyLevelMap()).thenReturn(
				hierarchyLevels2.stream().collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x)));
		Assert.assertEquals(filterHelperService.getHierarachyLevelId(4, "project", projectConfigScrum.isKanban()),
				"project");

	}

	@Test
	public void getFirstHierarachyLevel() {
		Assert.assertEquals(filterHelperService.getFirstHierarachyLevel(), "project");
	}

	@Test
	public void getHierarchyIdLevelMap() {
		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels2.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(cacheService.getFullHierarchyLevelMap()).thenReturn(hierarchyMap);
		Assert.assertEquals(filterHelperService.getHierarchyIdLevelMap(projectConfigScrum.isKanban()), map);

	}

	@Test
	public void getAdditionalFilterHierarchyLevel() {
		AdditionalFilterCategoryFactory additionalFilterCategoryFactory = AdditionalFilterCategoryFactory.newInstance();
		List<AdditionalFilterCategory> additionalFilterCategoryList = additionalFilterCategoryFactory
				.getAdditionalFilterCategoryList();
		Map<String, AdditionalFilterCategory> additonalFilterMap = additionalFilterCategoryList.stream()
				.collect(Collectors.toMap(AdditionalFilterCategory::getFilterCategoryId, x -> x));
		when(cacheService.getAdditionalFilterHierarchyLevel()).thenReturn(additonalFilterMap);
		Assert.assertEquals(filterHelperService.getAdditionalFilterHierarchyLevel(), additonalFilterMap);

	}

}