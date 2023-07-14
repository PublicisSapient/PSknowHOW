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

package com.publicissapient.kpidashboard.apis.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;

/**
 * A Service to manage cache.
 * 
 * @author anisingh4
 */
@Component
public interface CacheService {

	void clearCache(String cacheName);

	void clearAllCache();

	Object cacheAccountHierarchyData();

	Object cacheAccountHierarchyKanbanData();

	void setIntoApplicationCache(String key, String value);

	/**
	 * Stores the KPI data result. Cache key = key+requestOrigin+kpiSource. Given
	 * that none of them is empty.
	 * 
	 * @param key
	 *            mandatory parameter
	 * @param value
	 *            KPI result
	 * @param kpiSource
	 *            taken into account if not empty
	 * @param groupId
	 * @param sprintIncluded
	 *            sprintIncluded
	 */
	void setIntoApplicationCache(String[] key, Object value, String kpiSource, Integer groupId,
			List<String> sprintIncluded);

	/**
	 * Gets data from cache based on key
	 * 
	 * @param key
	 * @return
	 */
	String getFromApplicationCache(String key);

	/**
	 * Gets from the cache. Key formation strategy is same as detailed in the
	 * setIntoApplicationCache method above.
	 * 
	 * @param keyList
	 * @param kpiSource
	 * @param groupId
	 * @param sprintIncluded
	 *            sprintIncluded
	 * @return
	 */
	public Object getFromApplicationCache(String[] keyList, String kpiSource, Integer groupId,
			List<String> sprintIncluded);

	Object cacheProjectConfigMapData();

	Object cacheFieldMappingMapData();

	Object cacheToolConfigMapData();

	Object cacheProjectToolConfigMapData();

	List<HierarchyLevel> getFullHierarchyLevel();

	List<HierarchyLevel> getFullKanbanHierarchyLevel();

	Map<String, HierarchyLevel> getFullHierarchyLevelMap();

	Map<String, HierarchyLevel> getFullKanbanHierarchyLevelMap();

	Map<String, AdditionalFilterCategory> getAdditionalFilterHierarchyLevel();

}
