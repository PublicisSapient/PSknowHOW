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

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevelSuggestion;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.model.application.MaturityLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectBasicConfigNode;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfig;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingStructureRepository;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelSuggestionRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.impl.ProjectToolConfigRepositoryCustom;
import com.publicissapient.kpidashboard.common.repository.userboardconfig.UserBoardConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class for configuration
 *
 * @author anisingh4
 */
@Slf4j
@Service
public class ConfigHelperService {

	private final Map<ObjectId, Map<String, List<Tool>>> toolItemMap = new HashMap<>();
	@Autowired
	CacheService cacheService;
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;
	@Autowired
	private FieldMappingRepository fieldMappingRepository;
	@Autowired
	private ProjectToolConfigRepositoryCustom toolConfigRepository;
	@Autowired
	private KpiMasterRepository kpiMasterRepository;
	@Autowired
	private HierarchyLevelSuggestionRepository hierarchyLevelSuggestionRepository;
	@Autowired
	private ProjectBasicConfigService projectBasicConfigService;
	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;
	@Autowired
	private UserBoardConfigRepository userBoardConfigRepository;

	@Autowired
	private FieldMappingStructureRepository fieldMappingStructureRepository;
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private Map<ObjectId, Map<String, List<ProjectToolConfig>>> projectToolConfMap = new HashMap<>();

	/**
	 * Load project config list.
	 */
	public void loadConfigData() {
		log.info("loadConfigData - loading project config, field mapping and tool_config");
		projectConfigMap.clear();
		fieldMappingMap.clear();

		List<ProjectBasicConfig> projectList = projectConfigRepository.findAll();
		List<FieldMapping> fieldMappingList = fieldMappingRepository.findAll();

		projectList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getId().toString(), projectConfig);
			FieldMapping mapping = fieldMappingList.stream()
					.filter(x -> null != x.getBasicProjectConfigId()
							&& x.getBasicProjectConfigId().equals(projectConfig.getId()))
					.findAny().orElse(new FieldMapping());
			fieldMappingMap.put(projectConfig.getId(), mapping);
		});

	}

	/**
	 * This method load toolConfigMap
	 */
	public void loadToolConfig() {
		toolItemMap.clear();
		List<Tool> toolList = toolConfigRepository.getToolList();
		Map<ObjectId, List<Tool>> projectMap = toolList.stream().collect(Collectors.groupingBy(Tool::getProjectIds));
		projectMap.entrySet().forEach(project -> toolItemMap.put(project.getKey(),
				project.getValue().stream().collect(Collectors.groupingBy(Tool::getTool))));
	}

	/**
	 * This method load toolConfigMap
	 */
	public void loadProjectToolConfig() {
		projectToolConfMap.clear();
		List<ProjectToolConfig> toolList = projectToolConfigRepository.findAll();
		Map<ObjectId, List<ProjectToolConfig>> projectMap = toolList.stream()
				.collect(Collectors.groupingBy(ProjectToolConfig::getBasicProjectConfigId));
		projectMap.entrySet().forEach(project -> projectToolConfMap.put(project.getKey(),
				project.getValue().stream().collect(Collectors.groupingBy(ProjectToolConfig::getToolName))));
	}

	/**
	 * Gets tool item map. Map containing key as projectId and value as Map
	 * containing kay as Tool Name and value as List of configurations of that tool.
	 *
	 * @return the tool item map {@code  Map<ObjectId, Map<String, List<Tool>>>}
	 */
	public Map<ObjectId, Map<String, List<Tool>>> getToolItemMap() {
		return (Map<ObjectId, Map<String, List<Tool>>>) cacheService.cacheToolConfigMapData();
	}

	/**
	 * Gets projects tool config map. Map containing key as basicConfigId and value
	 * as Map containing kay as Project Tool Name and value as List of
	 * configurations of project tools.
	 *
	 * @return the project tool config map
	 *         {@code  Map<ObjectId, Map<String, List<Tool>>>}
	 */
	public Map<ObjectId, Map<String, List<ProjectToolConfig>>> getProjectToolConfigMap() {
		return (Map<ObjectId, Map<String, List<ProjectToolConfig>>>) cacheService.cacheProjectToolConfigMapData();
	}

	/**
	 * Gets project config.
	 *
	 * @param key
	 *            the key
	 * @return the project config
	 */
	public ProjectBasicConfig getProjectConfig(String key) {
		return getProjectConfigMap().get(key);
	}

	/**
	 * Gets field mapping.
	 *
	 * @param key
	 *            the key
	 * @return the field mapping
	 */
	public FieldMapping getFieldMapping(ObjectId key) {
		return getFieldMappingMap().get(key);
	}

	/**
	 * Gets project config map.
	 *
	 * @return the project config map
	 */
	public Map<String, ProjectBasicConfig> getProjectConfigMap() {
		return (Map<String, ProjectBasicConfig>) cacheService.cacheProjectConfigMapData();
	}

	/**
	 * Sets project config map.
	 *
	 * @param projectConfigMap
	 *            the project config map
	 */
	public void setProjectConfigMap(Map<String, ProjectBasicConfig> projectConfigMap) {
		this.projectConfigMap = projectConfigMap;
	}

	/**
	 * Gets field mapping map.
	 *
	 * @return the field mapping map
	 */
	public Map<ObjectId, FieldMapping> getFieldMappingMap() {
		return (Map<ObjectId, FieldMapping>) cacheService.cacheFieldMappingMapData();
	}

	/**
	 * Sets field mapping map.
	 *
	 * @param fieldMappingMap
	 *            the field mapping map
	 */
	public void setFieldMappingMap(Map<ObjectId, FieldMapping> fieldMappingMap) {
		this.fieldMappingMap = fieldMappingMap;
	}

	/**
	 * return Config data to cache service based on cache key
	 *
	 * @param key
	 * @return config object
	 */
	public Object getConfigMapData(String key) {
		switch (key) {
		case CommonConstant.CACHE_PROJECT_CONFIG_MAP:
			return projectConfigMap;
		case CommonConstant.CACHE_FIELD_MAPPING_MAP:
			return fieldMappingMap;
		case CommonConstant.CACHE_TOOL_CONFIG_MAP:
			return toolItemMap;
		case CommonConstant.CACHE_PROJECT_TOOL_CONFIG_MAP:
			return projectToolConfMap;
		default:
			return null;
		}
	}

	/**
	 * Load business unit Map.
	 */
	@Cacheable(CommonConstant.CACHE_KPI_MASTER)
	public Iterable<KpiMaster> loadKpiMaster() {
		log.info("loading KPI Master data");
		return kpiMasterRepository.findAll();
	}


	@Cacheable(CommonConstant.CACHE_MATURITY_RANGE)
	public Map<String, List<String>> calculateMaturity() {
		List<KpiMaster> masterList = (List<KpiMaster>) loadKpiMaster();

		Map<String, List<String>> kpiIdRangeMap = new HashMap<>();

		kpiIdRangeMap.putAll(masterList.stream().filter(d -> d.getMaturityRange() != null)
				.collect(Collectors.toMap(KpiMaster::getKpiId, KpiMaster::getMaturityRange)));

		return kpiIdRangeMap;

	}

	@Cacheable(CommonConstant.CACHE_AGG_CRITERIA)
	public Map<String, String> calculateCriteria() {
		List<KpiMaster> masterList = (List<KpiMaster>) loadKpiMaster();
		return masterList.stream().filter(d -> d.getAggregationCriteria() != null)
				.collect(Collectors.toMap(KpiMaster::getKpiId, KpiMaster::getAggregationCriteria));
	}

	@Cacheable(CommonConstant.CACHE_AGG_CIRCLE_CRITERIA)
	public Map<String, String> calculateCriteriaForCircleKPI() {
		List<KpiMaster> masterList = (List<KpiMaster>) loadKpiMaster();
		return masterList.stream().filter(d -> d.getAggregationCircleCriteria() != null)
				.collect(Collectors.toMap(KpiMaster::getKpiId, KpiMaster::getAggregationCircleCriteria));
	}

	@Cacheable(CommonConstant.CACHE_PROJECT_BASIC_TREE)
	public ProjectBasicConfigNode loadProjectBasicTree() {
		log.info("loading Project Basic Tree");
		return projectBasicConfigService.getBasicConfigTree();
	}

	/**
	 * Load cache hierarchy level value Map.
	 */
	@Cacheable(CommonConstant.CACHE_HIERARCHY_LEVEL_VALUE)
	public List<HierarchyLevelSuggestion> loadHierarchyLevelSuggestion() {
		log.info("loading hierarchy level Master data");
		return hierarchyLevelSuggestionRepository.findAll();
	}

	/**
	 * Load KPI Field Mapping.
	 */

	@Cacheable(CommonConstant.CACHE_FIELD_MAPPING_STUCTURE)
	public Object loadFieldMappingStructure() {
		log.info("loading FieldMappingStucture data");
		return fieldMappingStructureRepository.findAll();
	}

	@Cacheable(CommonConstant.CACHE_USER_BOARD_CONFIG)
	public List<UserBoardConfig> loadUserBoardConfig() {
		log.info("loading UserBoarConfig");
		return userBoardConfigRepository.findAll();
	}

	@Cacheable(CommonConstant.CACHE_PROJECT_TOOL_CONFIG)
	public Object loadAllProjectToolConfig() {
		log.info("loading projectToolConfig data");
		return projectToolConfigRepository.findAll();
	}

}
