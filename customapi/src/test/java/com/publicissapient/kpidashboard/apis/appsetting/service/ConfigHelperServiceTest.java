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

package com.publicissapient.kpidashboard.apis.appsetting.service;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.OrganizationHierarchyDataFactory;
import com.publicissapient.kpidashboard.apis.data.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectBasicConfigNode;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfig;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingStructureRepository;
import com.publicissapient.kpidashboard.common.repository.application.FiltersRepository;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.impl.ProjectToolConfigRepositoryCustom;
import com.publicissapient.kpidashboard.common.repository.userboardconfig.UserBoardConfigRepository;

@RunWith(MockitoJUnitRunner.class)
public class ConfigHelperServiceTest {

	@Mock
	CacheService cacheService;
	List<ProjectBasicConfig> projectList = null;
	List<FieldMapping> fieldMappingList = null;
	List<OrganizationHierarchy> organizationHierarchyList = null;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private FieldMappingRepository fieldMappingRepository;
	@Mock
	private ProjectToolConfigRepositoryCustom toolConfigRepository;
	@Mock
	UserBoardConfigRepository userBoardConfigRepository;
	@Mock
	FieldMappingStructureRepository fieldMappingStructureRepository;

	@Mock
	ProjectToolConfigRepository projectToolConfigRepository;
	@Mock
	OrganizationHierarchyRepository organizationHierarchyRepository;
	@Mock
	ProjectBasicConfigService projectBasicConfigService;
	@Mock
	KpiMasterRepository kpiMasterRepository;
	@InjectMocks
	private ConfigHelperService configHelperService;
	@Mock
	private FiltersRepository filtersRepository;
	@Mock
	private HierarchyLevelRepository hierarchyLevelRepository;

	@Before
	public void setUp() {
		projectList = ProjectBasicConfigDataFactory.newInstance("").getProjectBasicConfigs();
		fieldMappingList = FieldMappingDataFactory.newInstance("").getFieldMappings();
		organizationHierarchyList = OrganizationHierarchyDataFactory.newInstance("").getOrganizationHierarchies();
	}

	@Test
	public void loadConfigData() {
		Mockito.when(hierarchyLevelRepository.findAllByOrderByLevel()).thenReturn(new ArrayList<>());
		Mockito.when(projectConfigRepository.findAll()).thenReturn(projectList);
		Mockito.when(fieldMappingRepository.findAll()).thenReturn(fieldMappingList);
		configHelperService.loadConfigData();
		Assertions.assertTrue(((Map<String, ProjectBasicConfig>) configHelperService
				.getConfigMapData(CommonConstant.CACHE_PROJECT_CONFIG_MAP)).size() > 0);
		Assertions.assertTrue(((Map<ObjectId, FieldMapping>) configHelperService
				.getConfigMapData(CommonConstant.CACHE_FIELD_MAPPING_MAP)).size() > 0);
		Assertions.assertFalse(((Map<ObjectId, FieldMapping>) configHelperService
				.getConfigMapData(CommonConstant.CACHE_BOARD_META_DATA_MAP)).size() > 0);
	}

	@Test
	public void loadToolConfig() {
		List<Tool> toolList = new ArrayList<>();
		Tool tool1 = new Tool();
		tool1.setTool("scm");
		tool1.setBranch("master");
		tool1.setProjectIds(new ObjectId("5fd9ab0995fe13000165d0ba"));
		tool1.setUrl("TestURL");
		toolList.add(tool1);
		Mockito.when(toolConfigRepository.getToolList()).thenReturn(toolList);
		configHelperService.loadToolConfig();
		Assertions.assertTrue(
				((Map<ObjectId, Tool>) configHelperService.getConfigMapData(CommonConstant.CACHE_TOOL_CONFIG_MAP))
						.size() > 0);
	}

	@Test
	public void loadUserBoardConfig() {
		List<UserBoardConfig> userBoardConfigs = new ArrayList<>();
		UserBoardConfig userBoardConfig = new UserBoardConfig();
		userBoardConfig.setId(new ObjectId("5fd9ab0995fe13000165d0ba"));
		userBoardConfig.setUsername("PSK");
		userBoardConfigs.add(userBoardConfig);
		Mockito.when(userBoardConfigRepository.findAll()).thenReturn(userBoardConfigs);
		Assertions.assertEquals("PSK", configHelperService.loadUserBoardConfig().get(0).getUsername());
	}

	@Test
	public void loadFieldMappingStructure() {
		Mockito.when(fieldMappingStructureRepository.findAll()).thenReturn(new ArrayList<>());
		Assertions.assertNotNull(configHelperService.loadFieldMappingStructure().getClass());
	}

	@Test
	public void loadAllProjectToolConfig() {
		Mockito.when(projectToolConfigRepository.findAll()).thenReturn(new ArrayList<>());
		Assertions.assertNotNull(configHelperService.loadAllProjectToolConfig().getClass());
	}

	@Test
	public void loadProjectBasicTree() {
		ProjectBasicConfigNode projectBasicConfigNode = new ProjectBasicConfigNode();
		projectBasicConfigNode.setValue("Test");
		when(projectBasicConfigService.getBasicConfigTree()).thenReturn(projectBasicConfigNode);
		Assertions.assertNotNull(configHelperService.loadProjectBasicTree());
	}

	@Test
	public void calculateCriteriaForCircleKPI() {
		List<KpiMaster> kpiMasters = new ArrayList<>();
		KpiMaster kpiMaster = new KpiMaster();
		kpiMaster.setKpiId("5fd9ab0995fe13000165d0ba");
		kpiMaster.setAggregationCircleCriteria("criteriaX");
		kpiMasters.add(kpiMaster);
		Mockito.when(kpiMasterRepository.findAll()).thenReturn(kpiMasters);
		Assertions.assertEquals("criteriaX",
				configHelperService.calculateCriteriaForCircleKPI().get("5fd9ab0995fe13000165d0ba"));
	}

	@Test
	public void calculateCriteria() {
		List<KpiMaster> kpiMasters = new ArrayList<>();
		KpiMaster kpiMaster = new KpiMaster();
		kpiMaster.setKpiId("5fd9ab0995fe13000165d0ba");
		kpiMaster.setAggregationCriteria("criteriaX");
		kpiMasters.add(kpiMaster);
		Mockito.when(kpiMasterRepository.findAll()).thenReturn(kpiMasters);
		Assertions.assertEquals("criteriaX", configHelperService.calculateCriteria().get("5fd9ab0995fe13000165d0ba"));
	}

	@Test
	public void calculateMaturity() {
		List<KpiMaster> kpiMasters = new ArrayList<>();
		KpiMaster kpiMaster = new KpiMaster();
		kpiMaster.setKpiId("5fd9ab0995fe13000165d0ba");
		List<String> maturityRange = new ArrayList<>();
		maturityRange.add("criteriaX");
		kpiMaster.setMaturityRange(maturityRange);
		kpiMasters.add(kpiMaster);
		Mockito.when(kpiMasterRepository.findAll()).thenReturn(kpiMasters);
		Assertions.assertEquals("criteriaX",
				configHelperService.calculateMaturity().get("5fd9ab0995fe13000165d0ba").get(0));
	}

	@Test
	public void loadProjectToolConfigTest() {
		List<ProjectToolConfig> projectToolConfigs = new ArrayList<>();
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5fd9ab0995fe13000165d0ba"));
		projectToolConfig.setToolName("PSKnowHow");
		projectToolConfig.setProjectKey("123");
		projectToolConfigs.add(projectToolConfig);
		Mockito.when(projectToolConfigRepository.findAll()).thenReturn(projectToolConfigs);
		configHelperService.loadProjectToolConfig();
		Assertions.assertNotNull(projectToolConfigs);
	}

	@Test
	public void loadAllFilters() {
		when(filtersRepository.findAll()).thenReturn(null);
		Assertions.assertNull(configHelperService.loadAllFilters());
	}

	@Test
	public void loadAllOrganizationHierarchy() {
		when(organizationHierarchyRepository.findAll()).thenReturn(organizationHierarchyList);
		Assertions.assertNotNull(configHelperService.loadAllOrganizationHierarchy());
	}
}
