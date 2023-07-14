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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */

@Slf4j
public class ProjectReleaseDataFactory {

	private static final String FILE_PATH_KPI_REQUEST = "/json/default/project_release.json";
	private static final String FILE_PATH_PROJECT_VERSION = "/json/default/project_version.json";

	private ObjectMapper mapper = null;

	private List<ProjectRelease> projectRelease;
	private List<ProjectVersion> projectVersion;

	private ProjectReleaseDataFactory() {

	}

	public static ProjectReleaseDataFactory newInstance(String filePath, String versionFilePath) {

		ProjectReleaseDataFactory projectReleaseDataFactory = new ProjectReleaseDataFactory();
		projectReleaseDataFactory.createObjectMapper();
		projectReleaseDataFactory.createProjectVersion(versionFilePath);
		projectReleaseDataFactory.init(StringUtils.isEmpty(filePath) ? FILE_PATH_KPI_REQUEST : filePath);
		return projectReleaseDataFactory;
	}

	public static ProjectReleaseDataFactory newInstance() {
		return newInstance(null, null);
	}

	private void init(String filePath) {
		try {
			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_KPI_REQUEST : filePath;

			projectRelease = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<ProjectRelease>>() {
					});

			// projectRelease =
			// mapper.readValue(TypeReference.class.getResourceAsStream(filePath),
			// ProjectRelease.class);
		} catch (IOException e) {
			log.error("Error in reading kpi request from file = " + filePath, e);
		}
	}

	private void createProjectVersion(String versionFilePath) {
		try {
			String versionPath = StringUtils.isEmpty(versionFilePath) ? FILE_PATH_PROJECT_VERSION : versionFilePath;
			projectVersion = mapper.readValue(TypeReference.class.getResourceAsStream(versionPath),
					new TypeReference<List<ProjectVersion>>() {
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createObjectMapper() {

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.registerModule(new JodaModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}
	}

	public List<ProjectRelease> findByBasicProjectConfigId(String projectConfigId) {

		ProjectRelease projectRelease = this.projectRelease.stream().filter(
				projectRelease1 -> projectRelease1.getConfigId().toHexString().equalsIgnoreCase(projectConfigId))
				.findFirst().orElse(null);
		if (projectRelease != null) {
			projectRelease.setListProjectVersion(projectVersion);
		}
		List<ProjectRelease> list = new ArrayList<>();
		list.add(projectRelease);

		return list;
	}

}
