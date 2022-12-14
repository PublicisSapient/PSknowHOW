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

package com.publicissapient.kpidashboard.jira.config;//NOPMD

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Bean to hold settings specific to the Feature collector.
 */

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jira")
public class JiraProcessorConfig {
	// NOPMD
	// Do not remove pmd this is for ignoring TooManyFields violation
	// it is required

	private String cron;
	private int pageSize;
	private String startDate;
	private long minsToReduce;
	private String jiraBackLogStatusFlow;
	private String jsonFileName;
	private String customApiBaseUrl;
	private Integer socketTimeOut;
	private String estimationCriteria;
	private int threadPoolSize;
	@Value("${aesEncryptionKey}")
	private String aesEncryptionKey;
	private String jiraCloudGetUserApi;
	private String jiraServerGetUserApi;
	private boolean fetchMetadata;
	private List<String> excludeLinks;
	private String sprintJsonFileName;
	private List<String> rcaValuesForCodeIssue;
	private Integer sprintCountForCacheClean;
	private String jiraCloudSprintReportApi;
	private String jiraServerSprintReportApi;
	private String jiraDirectTicketLinkKey;
	private String jiraCloudDirectTicketLinkKey;
	private String jiraSprintByBoardUrlApi;
	private String jiraEpicApi;
	private Integer sprintReportCountToBeFetched;
	private boolean considerStartDate;
	private long subsequentApiCallDelayInMilli;
}