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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Slf4j
public class ProjectBasicConfigDataFactory {
	private static final String FILE_PATH_BASIC_CONFIG = "/json/default/project_basic_configs.json";

	private List<ProjectBasicConfig> projectBasicConfigs;
	private ObjectMapper mapper;

	private ProjectBasicConfigDataFactory() {
	}

	public static ProjectBasicConfigDataFactory newInstance(String filePath) {

		ProjectBasicConfigDataFactory factory = new ProjectBasicConfigDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_BASIC_CONFIG : filePath;

			projectBasicConfigs = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<ProjectBasicConfig>>() {
					});
		} catch (IOException e) {
			log.error("Error in reading project basic config from file = " + filePath, e);
		}
	}

	private void createObjectMapper() {

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

	}

	public List<ProjectBasicConfig> getProjectBasicConfigs() {
		return projectBasicConfigs;
	}

	public ProjectBasicConfig findById(String id) {

		return projectBasicConfigs.stream()
				.filter(projectBasicConfig -> projectBasicConfig.getId().toHexString().equals(id)).findFirst()
				.orElse(null);
	}

	public ProjectBasicConfig findByProjectName(String projectName) {

		return projectBasicConfigs.stream()
				.filter(projectBasicConfig -> projectBasicConfig.getProjectName().equals(projectName)).findFirst()
				.orElse(null);
	}

	public List<ProjectBasicConfig> findScrumProjects() {
		return projectBasicConfigs.stream().filter(projectBasicConfig -> !projectBasicConfig.getIsKanban())
				.collect(Collectors.toList());
	}

	public List<ProjectBasicConfig> findKanbanProjects() {
		return projectBasicConfigs.stream().filter(ProjectBasicConfig::getIsKanban).collect(Collectors.toList());
	}
}
