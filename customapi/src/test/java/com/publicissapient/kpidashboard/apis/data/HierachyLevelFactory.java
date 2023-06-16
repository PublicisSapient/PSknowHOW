/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Slf4j
public class HierachyLevelFactory {

	private static final String FILE_PATH_FILTER_CATEGORIES = "/json/default/hierarchy_levels.json";
	private List<HierarchyLevel> hierarchyLevels;
	private ObjectMapper mapper;

	private HierachyLevelFactory() {
	}

	public static HierachyLevelFactory newInstance(String filePath) {

		HierachyLevelFactory factory = new HierachyLevelFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static HierachyLevelFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_FILTER_CATEGORIES : filePath;

			hierarchyLevels = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<HierarchyLevel>>() {
					});
		} catch (IOException e) {
			log.error("Error in reading kpi request from file = " + filePath, e);
		}
	}

	private ObjectMapper createObjectMapper() {

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

		return mapper;
	}

	public List<HierarchyLevel> getHierarchyLevels() {
		return hierarchyLevels;
	}

	/*
	 * public List<String> getHierarchyOrderedByLevel() {
	 * 
	 * //return filterCategories.stream().filter(filterCategory ->
	 * filterCategory.getIsDeleted().equals("False"))
	 * .sorted(Comparator.comparingInt(FilterCategory::getLevel)).map(FilterCategory
	 * ::getCategoryName) .collect(Collectors.toList()); }
	 * 
	 * public List<FilterCategory> getHierarchyOrderByLevelAndEnabled() {
	 * 
	 * return filterCategories.stream() .filter(filterCategory ->
	 * filterCategory.getIsDeleted().equals("False") && filterCategory.isEnabled())
	 * .sorted(Comparator.comparingInt(FilterCategory::getLevel)).collect(Collectors
	 * .toList());
	 * 
	 * }
	 * 
	 * public String getFirstLevel() {
	 * 
	 * FilterCategory resultFilterCategory = filterCategories.stream().filter(
	 * filterCategory -> filterCategory.getIsDeleted().equals("False") &&
	 * filterCategory.getLevel() == 1) .findFirst().orElse(null); return
	 * resultFilterCategory == null ? null : resultFilterCategory.getCategoryName();
	 * }
	 */

}
