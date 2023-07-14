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

package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.filter.service.AccountHierarchyServiceImpl;
import com.publicissapient.kpidashboard.apis.filter.service.AccountHierarchyServiceKanbanImpl;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link CacheService}.
 * 
 * @author anisingh4
 */
@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

	@Autowired
	HierarchyLevelService hierarchyLevelService;
	@Autowired
	private AccountHierarchyServiceImpl accountHierarchyService;
	@Autowired
	private AccountHierarchyServiceKanbanImpl accountHierarchyServiceKanban;
	@Autowired
	@Qualifier("cacheManager")
	private CacheManager cacheManager;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private AdditionalFilterCategoryRepository additionalFilterCategoryRepository;

	@Override
	public void clearCache(String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		if (cache != null) {
			cache.clear();
			cache.evict(cacheName);
			log.info("Clearing Cache ==>> {}", cacheName);
		}
	}

	@Override
	public void clearAllCache() {
		cacheManager.getCacheNames().parallelStream().forEach(this::clearCache);
		log.info("<<==Clearing All Cache ==>>");
	}

	@Cacheable(CommonConstant.CACHE_ACCOUNT_HIERARCHY)
	@Override
	public Object cacheAccountHierarchyData() {
		return accountHierarchyService.createHierarchyData();

	}

	@Cacheable(CommonConstant.CACHE_ACCOUNT_HIERARCHY_KANBAN)
	@Override
	public Object cacheAccountHierarchyKanbanData() {
		return accountHierarchyServiceKanban.createHierarchyData();

	}

	@Cacheable(CommonConstant.CACHE_PROJECT_CONFIG_MAP)
	@Override
	public Object cacheProjectConfigMapData() {
		log.info("updating Project Config Cache");
		configHelperService.loadConfigData();
		return configHelperService.getConfigMapData(CommonConstant.CACHE_PROJECT_CONFIG_MAP);

	}

	@Cacheable(CommonConstant.CACHE_FIELD_MAPPING_MAP)
	@Override
	public Object cacheFieldMappingMapData() {
		log.info("updating FieldMapping Cache");
		configHelperService.loadConfigData();
		return configHelperService.getConfigMapData(CommonConstant.CACHE_FIELD_MAPPING_MAP);

	}

	@Cacheable(CommonConstant.CACHE_TOOL_CONFIG_MAP)
	@Override
	public Object cacheToolConfigMapData() {
		log.info("updating Tool Config Cache");
		configHelperService.loadToolConfig();
		return configHelperService.getConfigMapData(CommonConstant.CACHE_TOOL_CONFIG_MAP);

	}

	@Cacheable(CommonConstant.CACHE_PROJECT_TOOL_CONFIG_MAP)
	@Override
	public Object cacheProjectToolConfigMapData() {
		log.info("updating project Tool Config Cache");
		configHelperService.loadProjectToolConfig();
		return configHelperService.getConfigMapData(CommonConstant.CACHE_PROJECT_TOOL_CONFIG_MAP);

	}

	@Override
	public void setIntoApplicationCache(String key, String value) {
		Cache cache = cacheManager.getCache(CommonUtils.getCacheName(Constant.KPI_REQUEST_TRACKER_ID_KEY));
		if (null != cache) {
			cache.put(key, value);
		}
	}

	@Override
	public void setIntoApplicationCache(String[] keyList, Object value, String kpiSource, Integer groupId,
			List<String> sprintIncluded) {

		Arrays.sort(keyList);
		Cache cache = cacheManager.getCache(CommonUtils.getCacheName(kpiSource));
		if (null != cache) {
			StringBuilder keyBuilder = new StringBuilder();

			for (String string : keyList) {
				keyBuilder.append(string);
			}
			if (!kpiSource.isEmpty()) {
				keyBuilder.append(kpiSource);
			}
			if (groupId != null) {
				keyBuilder.append(groupId.toString());
			}
			if (CollectionUtils.isNotEmpty(sprintIncluded)) {
				sprintIncluded = sprintIncluded.stream().map(String::toLowerCase).sorted().collect(Collectors.toList());
				String sprintKey = String.join("", sprintIncluded);
				keyBuilder.append(sprintKey);
			}
			cache.put(keyBuilder.toString(), value);
		}
	}

	@Override
	public Object getFromApplicationCache(String[] keyList, String kpiSource, Integer groupId,
			List<String> sprintIncluded) {

		Arrays.sort(keyList);
		StringBuilder keyBuilder = new StringBuilder();
		for (String string : keyList) {
			keyBuilder.append(string);
		}

		if (!kpiSource.isEmpty()) {
			keyBuilder.append(kpiSource);
		}
		if (groupId != null) {
			keyBuilder.append(groupId.toString());
		}
		if (CollectionUtils.isNotEmpty(sprintIncluded)) {
			sprintIncluded = sprintIncluded.stream().map(String::toLowerCase).sorted().collect(Collectors.toList());
			String sprintKey = String.join("", sprintIncluded);
			keyBuilder.append(sprintKey);
		}
		Cache cache = cacheManager.getCache(CommonUtils.getCacheName(kpiSource));
		if (null == cache || null == cache.get(keyBuilder.toString())) {
			return null;
		}
		SimpleValueWrapper s = (SimpleValueWrapper) cache.get(keyBuilder.toString());
		if (s != null) {
			return s.get();
		}
		return null;
	}

	@Override
	public String getFromApplicationCache(String key) {
		Cache cache = cacheManager.getCache(CommonUtils.getCacheName(Constant.KPI_REQUEST_TRACKER_ID_KEY));
		if (null != cache) {
			SimpleValueWrapper s = (SimpleValueWrapper) cache.get(key);
			if (null != s) {
				Object s1 = s.get();
				if (s1 != null) {
					return s1.toString();
				}
			}
		}
		return "";
	}

	@Cacheable(Constant.CACHE_HIERARCHY_LEVEL)
	@Override
	public List<HierarchyLevel> getFullHierarchyLevel() {
		log.info("Caching Hierarchy level");
		return hierarchyLevelService.getFullHierarchyLevels(false);
	}

	@Cacheable(Constant.CACHE_KANBAN_HIERARCHY_LEVEL)
	@Override
	public List<HierarchyLevel> getFullKanbanHierarchyLevel() {
		log.info("Caching Kanban Hierarchy level");
		return hierarchyLevelService.getFullHierarchyLevels(true);
	}

	@Cacheable(Constant.CACHE_HIERARCHY_LEVEL_MAP)
	@Override
	public Map<String, HierarchyLevel> getFullHierarchyLevelMap() {
		log.info("Caching Hierarchy level Map");
		return getFullHierarchyLevel().stream().collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));

	}

	@Cacheable(Constant.CACHE_KANBAN_HIERARCHY_LEVEL_MAP)
	@Override
	public Map<String, HierarchyLevel> getFullKanbanHierarchyLevelMap() {
		log.info("Caching Hierarchy level kanban Map");
		return getFullKanbanHierarchyLevel().stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));

	}

	@Cacheable(Constant.CACHE_ADDITIONAL_FILTER_HIERARCHY_LEVEL)
	@Override
	public Map<String, AdditionalFilterCategory> getAdditionalFilterHierarchyLevel() {
		log.info("Caching Additional Filter Category Map");
		List<AdditionalFilterCategory> hierarchyLevels = additionalFilterCategoryRepository.findAll();
		return hierarchyLevels.stream()
				.collect(Collectors.toMap(AdditionalFilterCategory::getFilterCategoryId, x -> x));

	}
}