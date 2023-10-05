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

package com.publicissapient.kpidashboard.apis.cleanup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;

/**
 * @author anisingh4
 */
@Component
public class ToolDataCleanUpServiceFactory {

	private final Map<String, ToolDataCleanUpService> servicesCache = new HashMap<>();
	@Autowired
	private List<ToolDataCleanUpService> dataCleanUpServices;

	@PostConstruct
	public void initServices() {
		for (ToolDataCleanUpService dataCleanUpService : dataCleanUpServices) {
			servicesCache.put(dataCleanUpService.getToolCategory(), dataCleanUpService);
		}
	}

	/**
	 * Gets service object for the tool
	 * 
	 * @param toolName
	 *            name of the tool
	 * @return DataCleanUpService object for the tool
	 */
	public ToolDataCleanUpService getService(String toolName) {
		String toolCategory = getToolCategory(toolName);
		ToolDataCleanUpService dataCleanUpService = servicesCache.get(toolCategory);
		if (dataCleanUpService == null) {
			throw new NotImplementedException(
					"Not implemented for {tool = " + toolName + ", toolCategory = " + toolCategory + "}");
		}

		return dataCleanUpService;
	}

	private String getToolCategory(String toolName) {
		String toolCategory = "";

		switch (toolName) {
		case ProcessorConstants.JIRA:
		case ProcessorConstants.AZURE:
			toolCategory = ProcessorType.AGILE_TOOL.toString();
			break;
		case ProcessorConstants.JIRA_TEST:
		case ProcessorConstants.ZEPHYR:
			toolCategory = ProcessorType.TESTING_TOOLS.toString();
			break;
		case ProcessorConstants.SONAR:
			toolCategory = ProcessorType.SONAR_ANALYSIS.toString();
			break;
		case ProcessorConstants.BAMBOO:
		case ProcessorConstants.JENKINS:
		case ProcessorConstants.TEAMCITY:
		case ProcessorConstants.AZUREPIPELINE:
		case ProcessorConstants.GITHUBACTION:
			toolCategory = ProcessorType.BUILD.toString();
			break;
		case ProcessorConstants.BITBUCKET:
		case ProcessorConstants.GITLAB:
		case ProcessorConstants.AZUREREPO:
		case ProcessorConstants.REPO_TOOLS:
		case ProcessorConstants.GITHUB:
			toolCategory = ProcessorType.SCM.toString();
			break;
		case ProcessorConstants.NEWREILC:
			toolCategory = ProcessorType.NEW_RELIC.toString();
			break;
		default:
			throw new IllegalStateException("invalid tool name = " + toolName);

		}
		return toolCategory;
	}
}
