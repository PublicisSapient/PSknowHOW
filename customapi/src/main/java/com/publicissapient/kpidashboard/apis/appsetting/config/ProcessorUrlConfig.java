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

package com.publicissapient.kpidashboard.apis.appsetting.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "processorurl")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessorUrlConfig {

	String zephyr;
	String bamboo;
	String bitbucket;
	String gitlab;
	String excel;
	String jenkins;
	String jira;
	String jiraTest;
	String sonar;
	String azure;
	String azurepipeline;
	String azurerepository;
	String teamcity;
	String github;
	String githubAction;

	public String getProcessorUrl(String processor) {
		switch (processor) {
		case ProcessorConstants.ZEPHYR:
			return getZephyr();
		case ProcessorConstants.SONAR:
			return getSonar();
		case ProcessorConstants.BITBUCKET:
			return getBitbucket();
		case ProcessorConstants.GITLAB:
			return getGitlab();
		case ProcessorConstants.GITHUB:
			return getGithub();
		case ProcessorConstants.GITHUBACTION:
			return getGithubAction();
		case ProcessorConstants.EXCEL:
			return getExcel();
		case ProcessorConstants.BAMBOO:
			return getBamboo();
		case ProcessorConstants.JENKINS:
			return getJenkins();
		case ProcessorConstants.JIRA:
			return getJira();
		case ProcessorConstants.JIRA_TEST:
			return getJiraTest();
		case ProcessorConstants.AZURE:
			return getAzure();
		case ProcessorConstants.AZUREPIPELINE:
			return getAzurepipeline();
		case ProcessorConstants.AZUREREPO:
			return getAzurerepository();
		case ProcessorConstants.TEAMCITY:
			return getTeamcity();
		default:
			return StringUtils.EMPTY;
		}
	}
}
