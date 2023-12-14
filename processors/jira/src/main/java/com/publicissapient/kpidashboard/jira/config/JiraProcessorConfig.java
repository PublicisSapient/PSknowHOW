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
import java.util.Map;

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

	private int pageSize;
	private String customApiBaseUrl;
	private Integer socketTimeOut;
	private int threadPoolSize;
	@Value("${aesEncryptionKey}")
	private String aesEncryptionKey;
	private String jiraCloudGetUserApi;
	private String jiraServerGetUserApi;
	private boolean fetchMetadata;
	private List<String> excludeLinks;
	private List<String> rcaValuesForCodeIssue;
	private String jiraCloudSprintReportApi;
	private String jiraServerSprintReportApi;
	private String jiraDirectTicketLinkKey;
	private String jiraCloudDirectTicketLinkKey;
	private String jiraSprintByBoardUrlApi;
	private String jiraEpicApi;
	private Integer sprintReportCountToBeFetched;
	private boolean considerStartDate;
	private long subsequentApiCallDelayInMilli;

	@Value("${kafka.mailtopic}")
	private String kafkaMailTopic;
	private Map<String, String> notificationSubject;
	@Value("${notification.switch}")
	private boolean notificationSwitch;

	private Map<String, String> mailTemplate;

	@Value("${flag.mailWithoutKafka}")
	private boolean mailWithoutKafka;

	private String samlTokenStartString;
	private String samlTokenEndString;
	private String samlUrlStartString;
	private String samlUrlEndString;
	private String jiraVersionApi;
	private String jiraCloudVersionApi;
	private String jiraServerVersionReportApi;
	private String jiraCloudVersionReportApi;
	private Integer prevMonthCountToFetchData;
	private Integer daysToReduce;
	private Integer chunkSize;
	private String uiHost;
	private List<String> domainNames;
}