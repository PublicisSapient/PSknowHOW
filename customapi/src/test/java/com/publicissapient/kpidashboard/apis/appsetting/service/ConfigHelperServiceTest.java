package com.publicissapient.kpidashboard.apis.appsetting.service;

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
import com.publicissapient.kpidashboard.apis.data.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.impl.ProjectToolConfigRepositoryCustom;

@RunWith(MockitoJUnitRunner.class)
public class ConfigHelperServiceTest {

	@Mock
	CacheService cacheService;
	List<ProjectBasicConfig> projectList = null;
	List<FieldMapping> fieldMappingList = null;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private FieldMappingRepository fieldMappingRepository;
	@Mock
	private ProjectToolConfigRepositoryCustom toolConfigRepository;
	@InjectMocks
	private ConfigHelperService configHelperService;

	@Before
	public void setUp() {
		projectList = ProjectBasicConfigDataFactory.newInstance("").getProjectBasicConfigs();
		fieldMappingList = FieldMappingDataFactory.newInstance("").getFieldMappings();
	}

	@Test
	public void loadConfigData() {
		Mockito.when(projectConfigRepository.findAll()).thenReturn(projectList);
		Mockito.when(fieldMappingRepository.findAll()).thenReturn(fieldMappingList);
		configHelperService.loadConfigData();
		Assertions.assertTrue(((Map<String, ProjectBasicConfig>) configHelperService
				.getConfigMapData(CommonConstant.CACHE_PROJECT_CONFIG_MAP)).size() > 0);
		Assertions.assertTrue(((Map<ObjectId, FieldMapping>) configHelperService
				.getConfigMapData(CommonConstant.CACHE_FIELD_MAPPING_MAP)).size() > 0);
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
}
