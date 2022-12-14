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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelSuggestionRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiFieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.impl.ProjectToolConfigRepositoryCustom;

/**
 * Helper class for configuration
 * 
 * @author anisingh4
 */
@Service
public class ConfigHelperService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHelperService.class);
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
	private KpiFieldMappingRepository kpiFieldMappingRepository;
	@Autowired
	private HierarchyLevelSuggestionRepository hierarchyLevelSuggestionRepository;
	@Autowired
	private ProjectBasicConfigService projectBasicConfigService;
	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private Map<ObjectId, Map<String, List<ProjectToolConfig>>> projectToolConfMap = new HashMap<>();

	/**
	 * Load project config list.
	 */
	@PostConstruct
	public void loadConfigData() {
		LOGGER.info("loadConfigData - loading project config, field mapping and tool_config");
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
	@PostConstruct
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
	@PostConstruct
	@Cacheable(CommonConstant.CACHE_KPI_MASTER)
	public Iterable<KpiMaster> loadKpiMaster() {
		LOGGER.info("loading KPI Master data");
		return kpiMasterRepository.findAll();
	}

	@PostConstruct
	@Cacheable(CommonConstant.CACHE_MATURITY_RANGE)
	public Map<String, List<String>> calculateMaturity() {
		List<KpiMaster> masterList = (List<KpiMaster>) loadKpiMaster();

		Map<String, List<String>> kpiIdRangeMap = new HashMap<>();

		kpiIdRangeMap.putAll(masterList.stream().filter(d -> d.getMaturityRange() != null)
				.collect(Collectors.toMap(KpiMaster::getKpiId, KpiMaster::getMaturityRange)));

		masterList.stream().filter(d -> d.getMaturityLevel() != null)
				.forEach(master -> kpiIdRangeMap.putAll(master.getMaturityLevel().stream()
						.collect(Collectors.toMap(MaturityLevel::getLevel, MaturityLevel::getRange)))

				);

		return kpiIdRangeMap;

	}

	@PostConstruct
	@Cacheable(CommonConstant.CACHE_AGG_CRITERIA)
	public Map<String, String> calculateCriteria() {
		List<KpiMaster> masterList = (List<KpiMaster>) loadKpiMaster();
		return masterList.stream().filter(d -> d.getAggregationCriteria() != null)
				.collect(Collectors.toMap(KpiMaster::getKpiId, KpiMaster::getAggregationCriteria));
	}

	@PostConstruct
	@Cacheable(CommonConstant.CACHE_PROJECT_BASIC_TREE)
	public ProjectBasicConfigNode loadProjectBasicTree() {
		LOGGER.info("loading Project Basic Tree");
		return projectBasicConfigService.getBasicConfigTree();
	}

	/**
	 * Load cache hierarchy level value Map.
	 */
	@PostConstruct
	@Cacheable(CommonConstant.CACHE_HIERARCHY_LEVEL_VALUE)
	public List<HierarchyLevelSuggestion> loadHierarchyLevelSuggestion() {
		LOGGER.info("loading hierarchy level Master data");
		return hierarchyLevelSuggestionRepository.findAll();
	}

	/**
	 * Load KPI Field Mapping.
	 */
	@PostConstruct
	@Cacheable(CommonConstant.CACHE_KPI_FIELD_MAPPING)
	public Object loadKpiFieldMapping() {
		LOGGER.info("loading KPI FieldMapping data");
		return kpiFieldMappingRepository.findAll();
	}

}
