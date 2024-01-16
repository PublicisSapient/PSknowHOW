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

package com.publicissapient.kpidashboard.apis.config;//NOPMD // do not remove NOPMD comment it ignores ExcessivePublicCount

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * This class is used to bind external configurations to a bean in application
 * code. You can inject and use this bean throughout your application code just
 * like any other spring bean.
 * <p>
 * Properties defined in CustomApi.properties files are bind with this bean in
 * CustomApiApplication.
 *
 * @author pankumar8
 */
@Component
@ConfigurationProperties
public class CustomApiConfig {// NOPMD
	// Do not remove NOPMD comment. This is required to ignore TooManyFields.

	// AES key used to encrypt
	private String aesEncryptionKey;
	// Number of sprints available on trend charts
	private int sprintCountForFilters;
	// Toggle captcha property
	private boolean captchaRequired;
	// default image name
	private String applicationDefaultLogo;

	// priority kpi wise. Key : priority. Value: priority value
	private Map<String, List<String>> priority;
	// Defect Aging x axis values list
	private List<String> totalDefectCountAgingXAxisRange;

	// if aggregation criteria is percentile provide percentile value. Default
	// 90.
	// if aggregation criteria is percentile provide percentile value. Default
	// 90.
	private Double percentileValue;
	// repo x axis count days ranges
	private Integer repoXAxisCount;

	// Cors Enables toggle
	@Value("#{new Boolean('${corsEnabled:false}')}")
	private boolean corsEnabled;
	// Cors whitelist ip
	private String corsWhitelist;
	@Value("${forgotPassword.expiryInterval}")
	private String forgotPasswordExpiryInterval;
	// forgot password email subject
	@Value("${forgotPassword.emailSubject}")
	private String emailSubject;
	// forgot password server host only for server where nginex is not
	// setup
	@Value("${forgotPassword.serverPort}")
	private String serverPort;
	// forgot password server host only for server where nginex is not
	// setup
	@Value("${forgotPassword.uiHost}")
	private String uiHost;
	// forgot password UI port only for server where nginex is not setup
	@Value("${forgotPassword.uiPort}")
	private String uiPort;
	// detailed logger property toggle
	private String applicationDetailedLogger;
	// white list of origin values to be allowed under CORS
	private List<String> corsFilterValidOrigin;

	// Maximum Pending Requests allowed Per Username
	private Integer maxPendingRequestsPerUsername;
	@Value("${filter.date.range.show:true}")
	private boolean showDateRangeFilter;

	@Value("${testconnection.jiraApi}")
	private String jiraTestConnection;

	@Value("${testconnection.sonarApi}")
	private String sonarTestConnection;

	@Value("${testconnection.teamcityApi}")
	private String teamcityTestConnection;

	@Value("${testconnection.bambooApi}")
	private String bambooTestConnection;

	@Value("${testconnection.jenkinsApi}")
	private String jenkinsTestConnection;

	@Value("${testconnection.bitbucketApi}")
	private String bitbucketTestConnection;

	@Value("${testconnection.azureBoardApi}")
	private String azureBoardApi;

	@Value("${testconnection.azureRepoApi}")
	private String azureRepoApi;

	@Value("${testconnection.azurePipelineApi}")
	private String azurePipelineApi;

	@Value("${testconnection.zephyrApi}")
	private String zephyrTestConnection;

	@Value("${testconnection.gitlabApi}")
	private String gitlabTestConnection;

	@Value("${priority.P1}")
	private String priorityP1;

	@Value("${priority.P2}")
	private String priorityP2;

	@Value("${priority.P3}")
	private String priorityP3;

	@Value("${priority.P4}")
	private String priorityP4;
	@Value("p5-trivial, 5, trivial")
	private String priorityP5;
	@Value("$spring.kafka.producer.bootstrap-servers")
	private List<String> kafkaProducerBootStrapServers;
	@Value("${kafka.mailtopic}")
	private String kafkaMailTopic;
	private Map<String, String> notificationSubject;
	@Value("${notification.switch}")
	private boolean notificationSwitch;
	@Value("${analytics.switch}")
	private boolean analyticsSwitch;
	// feedback categories
	@Value("${feedback.categories}")
	private List<String> feedbackCategories;
	// Subject For Feedback Email
	@Value("${feedback.categories.emailSubject}")
	private String feedbackEmailSubject;
	@Value("${approval.categories.emailSubject}")
	private String approvalEmailSubject;

	@Value("${issueCount.story.categories}")
	private List<String> issueCountStoryCategories;

	@Value("${certhostpath}")
	private String hostPath;

	private int sonarWeekCount;
	private int jenkinsWeekCount;
	private int authCookieDuration;
	private boolean authCookieHttpOnly;
	private boolean authCookieSecured;
	private String authCookieSameSite;
	private int hierarchySelectionCount; // get hierachySelection Count, by default 3
	@Value("${dateRangeFilter.types}")
	private List<String> dateRangeFilterTypes; // get type of date for Kanban Date Filter
	@Value("${dateRangeFilter.counts}")
	private List<Integer> dateRangeFilterCounts; // get counts of date type for Kanban Date Filter

	@Value("${capacity.kanban.numberOfPastWeeks}")
	private int numberOfPastWeeksForKanbanCapacity;

	@Value(("${capacity.kanban.numberOfFutureWeeks}"))
	private int numberOfFutureWeeksForKanbanCapacity;

	@Value(("${testExecution.kanban.numberOfPastDays}"))
	private int numberOfPastDaysForKanbanTestExecution;

	@Value(("${testExecution.kanban.numberOfFutureDays}"))
	private int numberOfFutureDaysForKanbanTestExecution;

	@Value("${kpi.comments.max.store.count}")
	private int kpiCommentsMaxStoreCount;

	@Value("${latest.kpi.comments.summary}")
	private int latestKpiCommentsSummary;

	private int jiraXaxisMonthCount;

	private boolean ssoLogin;

	private Map<String, String> mailTemplate;

	@Value("${flag.mailWithoutKafka}")
	private boolean mailWithoutKafka;

	@Value(("${backlog.sprint.count}"))
	private int sprintCountForBackLogStrength;

	@Value("${sprint.velocity.limit}")
	private int sprintVelocityLimit;
	private int pushDataLimit;// limit of data allowed to push through api
	private int exposeAPITokenExpiryDays; // expose api token expiry after days limit
	@Value("${backlogWeekCount}")
	private int backlogWeekCount;
	@Value("${flowKpiMonthCount}")
	private int flowKpiMonthCount;
	private String samlTokenStartString;
	private String samlTokenEndString;
	private String samlUrlStartString;
	private String samlUrlEndString;
	// repo x axis count days rangeForCheckInsAndMergeRequests
	private Integer repoXAxisCountForCheckInsAndMergeRequests;
	private String repoToolAPIKey;
	private String repoToolURL;
	private Boolean isRepoToolEnable;
	private String repoToolCodeCommmitsUrl;
	private String repoToolDeleteProjectUrl;
	private String repoToolDeleteRepoUrl;
	private String repoToolTriggerScan;

	private String repoToolEnrollProjectUrl;
	private String repoToolPRSizeUrl;
	private String repoToolPickupTimeUrl;
	private String repoToolMeanTimeToMergeUrl;
	private List<String> flowEfficiencyXAxisRange;
	private List<String> leadTimeRange;
	private List<String> cycleTimeRange;
	private List<Character> aesKeyValue;

	public List<Character> getAesKeyValue() {
		return aesKeyValue;
	}

	public void setAesKeyValue(List<Character> aesKeyValue) {
		this.aesKeyValue = aesKeyValue;
	}

	public List<String> getCycleTimeRange() {
		return cycleTimeRange;
	}

	public void setCycleTimeRange(List<String> cycleTimeRange) {
		this.cycleTimeRange = cycleTimeRange;
	}



	public List<String> getLeadTimeRange() {
		return leadTimeRange;
	}

	public void setLeadTimeRange(List<String> leadTimeRange) {
		this.leadTimeRange = leadTimeRange;
	}

	public void setFlowEfficiencyXAxisRange(List<String> flowEfficiencyXAxisRange) {
		this.flowEfficiencyXAxisRange = flowEfficiencyXAxisRange;
	}

	public List<String> getFlowEfficiencyXAxisRange() {
		return flowEfficiencyXAxisRange;
	}

	public void setRepoToolAPIKey(String repoToolAPIKey) {
		this.repoToolAPIKey = repoToolAPIKey;
	}

	public void setRepoToolURL(String repoToolURL) {
		this.repoToolURL = repoToolURL;
	}

	public void setRepoToolPRSizeUrl(String repoToolPRSizeUrl) {
		this.repoToolPRSizeUrl = repoToolPRSizeUrl;
	}

	public void setRepoToolPickupTimeUrl(String repoToolPickupTimeUrl) {
		this.repoToolPickupTimeUrl = repoToolPickupTimeUrl;
	}

	public void setRepoToolMeanTimeToMergeUrl(String repoToolMeanTimeToMergeUrl) {
		this.repoToolMeanTimeToMergeUrl = repoToolMeanTimeToMergeUrl;
	}

	public void setRepoToolCodeCommmitsUrl(String repoToolCodeCommmitsUrl) {
		this.repoToolCodeCommmitsUrl = repoToolCodeCommmitsUrl;
	}

	public void setRepoToolDeleteProjectUrl(String repoToolDeleteProjectUrl) {
		this.repoToolDeleteProjectUrl = repoToolDeleteProjectUrl;
	}

	public void setRepoToolDeleteRepoUrl(String repoToolDeleteRepoUrl) {
		this.repoToolDeleteRepoUrl = repoToolDeleteRepoUrl;
	}

	public void setRepoToolTriggerScan(String repoToolTriggerScan) {
		this.repoToolTriggerScan = repoToolTriggerScan;
	}

	public void setRepoToolEnrollProjectUrl(String repoToolEnrollProjectUrl) {
		this.repoToolEnrollProjectUrl = repoToolEnrollProjectUrl;
	}

	public String getRepoToolTriggerScan() {
		return repoToolTriggerScan;
	}

	public String getRepoToolEnrollProjectUrl() {
		return repoToolEnrollProjectUrl;
	}

	public String getRepoToolPRSizeUrl() {
		return repoToolPRSizeUrl;
	}

	public String getRepoToolPickupTimeUrl() {
		return repoToolPickupTimeUrl;
	}

	public String getRepoToolMeanTimeToMergeUrl() {
		return repoToolMeanTimeToMergeUrl;
	}

	public String getRepoToolCodeCommmitsUrl() {
		return repoToolCodeCommmitsUrl;
	}

	public String getRepoToolDeleteProjectUrl() {
		return repoToolDeleteProjectUrl;
	}

	public String getRepoToolDeleteRepoUrl() {
		return repoToolDeleteRepoUrl;
	}

	public Boolean getIsRepoToolEnable() {
		return isRepoToolEnable;
	}

	public void setIsRepoToolEnable(Boolean isRepoToolEnable) {
		this.isRepoToolEnable = isRepoToolEnable;
	}

	public String getRepoToolAPIKey() {
		return repoToolAPIKey;
	}

	public String getRepoToolURL() {
		return repoToolURL;
	}

	private Integer sonarMonthCount;

	public int getSprintVelocityLimit() {
		return sprintVelocityLimit;
	}

	public void setSprintVelocityLimit(int sprintVelocityLimit) {
		this.sprintVelocityLimit = sprintVelocityLimit;
	}

	public int getLatestKpiCommentsSummary() {
		return latestKpiCommentsSummary;
	}

	public void setLatestKpiCommentsSummary(int latestKpiCommentsSummary) {
		this.latestKpiCommentsSummary = latestKpiCommentsSummary;
	}

	public int getKpiCommentsMaxStoreCount() {
		return kpiCommentsMaxStoreCount;
	}

	public void setKpiCommentsMaxStoreCount(int kpiCommentsMaxStoreCount) {
		this.kpiCommentsMaxStoreCount = kpiCommentsMaxStoreCount;
	}

	public int getPushDataLimit() {
		return pushDataLimit;
	}

	public void setPushDataLimit(int pushDataLimit) {
		this.pushDataLimit = pushDataLimit;
	}

	public int getExposeAPITokenExpiryDays() {
		return exposeAPITokenExpiryDays;
	}

	public void setExposeAPITokenExpiryDays(int exposeAPITokenExpiryDays) {
		this.exposeAPITokenExpiryDays = exposeAPITokenExpiryDays;
	}

	public int getBacklogWeekCount() {
		return this.backlogWeekCount;
	}

	public int getJiraXaxisMonthCount() {
		return jiraXaxisMonthCount;
	}

	public void setJiraXaxisMonthCount(int jiraXaxisMonthCount) {
		this.jiraXaxisMonthCount = jiraXaxisMonthCount;
	}

	public List<String> getDateRangeFilterTypes() {
		return dateRangeFilterTypes;
	}

	public void setDateRangeFilterTypes(List<String> dateRangeFilterTypes) {
		this.dateRangeFilterTypes = dateRangeFilterTypes;
	}

	public List<Integer> getDateRangeFilterCounts() {
		return dateRangeFilterCounts;
	}

	public void setDateRangeFilterCounts(List<Integer> dateRangeFilterCounts) {
		this.dateRangeFilterCounts = dateRangeFilterCounts;
	}

	public String getPriorityP5() {
		return priorityP5;
	}

	public void setPriorityP5(String priorityP5) {
		this.priorityP5 = priorityP5;
	}

	public String getApprovalEmailSubject() {
		return approvalEmailSubject;
	}

	public void setApprovalEmailSubject(String approvalEmailSubject) {
		this.approvalEmailSubject = approvalEmailSubject;
	}

	public int getHierarchySelectionCount() {
		return hierarchySelectionCount;
	}

	public void setHierarchySelectionCount(int hierarchySelectionCount) {
		this.hierarchySelectionCount = hierarchySelectionCount;
	}

	/**
	 * @return feedbackCategories
	 */
	public List<String> getFeedbackCategories() {
		return this.feedbackCategories;
	}

	/**
	 * @param feedbackCategories
	 *            new value of {@link #feedbackCategories}.
	 */
	public void setFeedbackCategories(List<String> feedbackCategories) {
		this.feedbackCategories = feedbackCategories;
	}

	/**
	 * @return feedbackEmailSubject
	 */
	public String getFeedbackEmailSubject() {
		return this.feedbackEmailSubject;
	}

	/**
	 * @param feedbackEmailSubject
	 *            new value of {@link #feedbackEmailSubject}.
	 */
	public void setFeedbackEmailSubject(String feedbackEmailSubject) {
		this.feedbackEmailSubject = feedbackEmailSubject;
	}

	/**
	 * get aesEncryptionKey
	 *
	 * @return the aesEncryptionKey
	 */
	public String getAesEncryptionKey() {
		return aesEncryptionKey;
	}

	/**
	 * set aesEncryptionKey
	 *
	 * @param aesEncryptionKey
	 *            the aesEncryptionKey to set
	 */
	public void setAesEncryptionKey(String aesEncryptionKey) {
		this.aesEncryptionKey = aesEncryptionKey;
	}

	/**
	 * get sprintCountForFilters, number of sprints to be shown per
	 * project/sub-project
	 *
	 * @return the sprintCountForFilters
	 */
	public int getSprintCountForFilters() {
		return sprintCountForFilters;
	}

	/**
	 * set sprintCountForFilters, number of sprints to be shown per
	 * project/sub-project
	 *
	 * @param sprintCountForFilters
	 *            the sprintCountForFilters to set
	 */
	public void setSprintCountForFilters(int sprintCountForFilters) {
		this.sprintCountForFilters = sprintCountForFilters;
	}

	/**
	 * get captchaRequired
	 *
	 * @return the captchaRequired
	 */
	public boolean isCaptchaRequired() {
		return captchaRequired;
	}

	/**
	 * set captchaRequired
	 *
	 * @param captchaRequired
	 *            the captchaRequired to set
	 */
	public void setCaptchaRequired(boolean captchaRequired) {
		this.captchaRequired = captchaRequired;
	}

	/**
	 * get applicationDefaultLogo
	 *
	 * @return the applicationDefaultLogo
	 */
	public String getApplicationDefaultLogo() {
		return applicationDefaultLogo;
	}

	/**
	 * set applicationDefaultLogo
	 *
	 * @param applicationDefaultLogo
	 *            the applicationDefaultLogo to set
	 */
	public void setApplicationDefaultLogo(String applicationDefaultLogo) {
		this.applicationDefaultLogo = applicationDefaultLogo;
	}

	/**
	 * get totalDefectCountAgingXAxisRange
	 *
	 * @return the totalDefectCountAgingXAxisRange
	 */
	public List<String> getTotalDefectCountAgingXAxisRange() {
		return totalDefectCountAgingXAxisRange;
	}

	/**
	 * set totalDefectCountAgingXAxisRange
	 *
	 * @param totalDefectCountAgingXAxisRange
	 *            the totalDefectCountAgingXAxisRange to set
	 */
	public void setTotalDefectCountAgingXAxisRange(List<String> totalDefectCountAgingXAxisRange) {
		this.totalDefectCountAgingXAxisRange = totalDefectCountAgingXAxisRange;
	}

	/**
	 * get percentileValue
	 *
	 * @return the percentileValue
	 */
	public Double getPercentileValue() {
		return percentileValue;
	}

	/**
	 * set percentileValue
	 *
	 * @param percentileValue
	 *            the percentileValue to set
	 */
	public void setPercentileValue(Double percentileValue) {
		this.percentileValue = percentileValue;
	}

	/**
	 * get repoXAxisCount
	 *
	 * @return the repoXAxisCount
	 */
	public Integer getRepoXAxisCount() {
		return repoXAxisCount;
	}

	/**
	 * set repoXAxisCount
	 *
	 * @param repoXAxisCount
	 *            the repoXAxisCount to set
	 */
	public void setRepoXAxisCount(Integer repoXAxisCount) {
		this.repoXAxisCount = repoXAxisCount;
	}

	/**
	 * get corsEnabled
	 *
	 * @return the corsEnabled
	 */
	public boolean isCorsEnabled() {
		return corsEnabled;
	}

	/**
	 * set corsEnabled
	 *
	 * @param corsEnabled
	 *            the corsEnabled to set
	 */
	public void setCorsEnabled(boolean corsEnabled) {
		this.corsEnabled = corsEnabled;
	}

	/**
	 * get corsWhitelist
	 *
	 * @return the corsWhitelist
	 */
	public String getCorsWhitelist() {
		return corsWhitelist;
	}

	/**
	 * set corsWhitelist
	 *
	 * @param corsWhitelist
	 *            the corsWhitelist to set
	 */
	public void setCorsWhitelist(String corsWhitelist) {
		this.corsWhitelist = corsWhitelist;
	}

	/**
	 * get forgotPasswordExpiryInterval
	 *
	 * @return the forgotPasswordExpiryInterval
	 */
	public String getForgotPasswordExpiryInterval() {
		return forgotPasswordExpiryInterval;
	}

	/**
	 * set forgotPasswordExpiryInterval
	 *
	 * @param forgotPasswordExpiryInterval
	 *            the forgotPasswordExpiryInterval to set
	 */
	public void setForgotPasswordExpiryInterval(String forgotPasswordExpiryInterval) {
		this.forgotPasswordExpiryInterval = forgotPasswordExpiryInterval;
	}

	/**
	 * get emailSubject
	 *
	 * @return the emailSubject
	 */
	public String getEmailSubject() {
		return emailSubject;
	}

	/**
	 * set emailSubject
	 *
	 * @param emailSubject
	 *            the emailSubject to set
	 */
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	/**
	 * get serverPort
	 *
	 * @return the serverPort
	 */
	public String getServerPort() {
		return serverPort;
	}

	/**
	 * set serverPort
	 *
	 * @param serverPort
	 *            the serverPort to set
	 */
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * get uiHost
	 *
	 * @return the uiHost
	 */
	public String getUiHost() {
		return uiHost;
	}

	/**
	 * set uiHost
	 *
	 * @param uiHost
	 *            the uiHost to set
	 */
	public void setUiHost(String uiHost) {
		this.uiHost = uiHost;
	}

	/**
	 * get uiPort
	 *
	 * @return the uiPort
	 */
	public String getUiPort() {
		return uiPort;
	}

	/**
	 * set uiPort
	 *
	 * @param uiPort
	 *            the uiPort to set
	 */
	public void setUiPort(String uiPort) {
		this.uiPort = uiPort;
	}

	/**
	 * get applicationDetailedLogger
	 *
	 * @return the applicationDetailedLogger
	 */
	public String getApplicationDetailedLogger() {
		return applicationDetailedLogger;
	}

	/**
	 * set applicationDetailedLogger
	 *
	 * @param applicationDetailedLogger
	 *            the applicationDetailedLogger to set
	 */
	public void setApplicationDetailedLogger(String applicationDetailedLogger) {
		this.applicationDetailedLogger = applicationDetailedLogger;
	}

	/**
	 * <p>
	 * Returns the list of values valid as origin
	 * </p>
	 *
	 * @return cors filter valid origin
	 */
	public List<String> getCorsFilterValidOrigin() {
		return corsFilterValidOrigin;
	}

	/**
	 * <p>
	 * Sets the list of values valid as origin
	 * </p>
	 *
	 * @param corsFilterValidOrigin
	 *            the cors filter valid origin
	 */
	public void setCorsFilterValidOrigin(List<String> corsFilterValidOrigin) {
		this.corsFilterValidOrigin = corsFilterValidOrigin;
	}

	public List<String> getIssueCountStoryCategories() {
		return issueCountStoryCategories;
	}

	public void setIssueCountStoryCategories(List<String> issueCountStoryCategories) {
		this.issueCountStoryCategories = issueCountStoryCategories;
	}

	/**
	 * get maxPendingRequestsPerUsername
	 *
	 * @return the max pending requests per username
	 */
	public Integer getMaxPendingRequestsPerUsername() {
		return maxPendingRequestsPerUsername;
	}

	/**
	 * set maxPendingRequestsPerUsername
	 *
	 * @param maxPendingRequestsPerUsername
	 *            the maxPendingRequestsPerUsername to set
	 */
	public void setMaxPendingRequestsPerUsername(Integer maxPendingRequestsPerUsername) {
		this.maxPendingRequestsPerUsername = maxPendingRequestsPerUsername;
	}

	public String getZephyrTestConnection() {
		return zephyrTestConnection;
	}

	/**
	 * JIRA Test connection API
	 *
	 * @return
	 */
	public String getJiraTestConnection() {
		return jiraTestConnection;
	}

	/**
	 * Sonar Test connection API
	 *
	 * @return
	 */
	public String getSonarTestConnection() {
		return sonarTestConnection;
	}

	/**
	 * teamcity Test connection API
	 *
	 * @return
	 */
	public String getTeamcityTestConnection() {
		return teamcityTestConnection;
	}

	/**
	 * bamboo Test connection API
	 *
	 * @return
	 */
	public String getBambooTestConnection() {
		return bambooTestConnection;
	}

	/**
	 * Jenkins Test connection API
	 *
	 * @return
	 */
	public String getJenkinsTestConnection() {
		return jenkinsTestConnection;
	}

	/**
	 * Bitbucket Test Connection API path
	 *
	 * @return
	 */
	public String getBitbucketTestConnection() {
		return bitbucketTestConnection;
	}

	/**
	 * P4 priority
	 *
	 * @return
	 */
	public String getpriorityP4() {
		return priorityP4;
	}

	/**
	 * P3 priority
	 *
	 * @return
	 */
	public String getpriorityP3() {
		return priorityP3;
	}

	/**
	 * P2 priority
	 *
	 * @return
	 */
	public String getpriorityP2() {
		return priorityP2;
	}

	/**
	 * P1 priority
	 *
	 * @return
	 */
	public String getpriorityP1() {
		return priorityP1;
	}

	/**
	 *
	 * @return emmStatsMonth
	 */

	public String getGitlabTestConnection() {
		return gitlabTestConnection;
	}

	/**
	 *
	 * @return String
	 */
	public String getKafkaMailTopic() {
		return kafkaMailTopic;
	}

	/**
	 *
	 * @param kafkaMailTopic
	 */
	public void setKafkaMailTopic(String kafkaMailTopic) {
		this.kafkaMailTopic = kafkaMailTopic;
	}

	public List<String> getKafkaProducerBootStrapServers() {
		return kafkaProducerBootStrapServers;
	}

	public void setKafkaProducerBootStrapServers(List<String> kafkaProducerBootStrapServers) {
		this.kafkaProducerBootStrapServers = kafkaProducerBootStrapServers;
	}

	public Map<String, String> getNotificationSubject() {
		return notificationSubject;
	}

	public void setNotificationSubject(Map<String, String> notificationSubject) {
		this.notificationSubject = notificationSubject;
	}

	public boolean isNotificationSwitch() {
		return notificationSwitch;
	}

	public void setNotificationSwitch(boolean notificationSwitch) {
		this.notificationSwitch = notificationSwitch;
	}

	public boolean isAnalyticsSwitch() {
		return analyticsSwitch;
	}

	public String getAzureBoardApi() {
		return azureBoardApi;
	}

	public void setAzureBoardApi(String azureBoardApi) {
		this.azureBoardApi = azureBoardApi;
	}

	public String getAzureRepoApi() {
		return azureRepoApi;
	}

	public void setAzureRepoApi(String azureRepoApi) {
		this.azureRepoApi = azureRepoApi;
	}

	public String getAzurePipelineApi() {
		return azurePipelineApi;
	}

	public void setAzurePipelineApi(String azurePipelineApi) {
		this.azurePipelineApi = azurePipelineApi;
	}

	public int getAuthCookieDuration() {
		return authCookieDuration;
	}

	public void setAuthCookieDuration(int authCookieDuration) {
		this.authCookieDuration = authCookieDuration;
	}

	public boolean isAuthCookieHttpOnly() {
		return authCookieHttpOnly;
	}

	public void setAuthCookieHttpOnly(boolean authCookieHttpOnly) {
		this.authCookieHttpOnly = authCookieHttpOnly;
	}

	public boolean isAuthCookieSecured() {
		return authCookieSecured;
	}

	public void setAuthCookieSecured(boolean authCookieSecured) {
		this.authCookieSecured = authCookieSecured;
	}

	public String getAuthCookieSameSite() {
		return authCookieSameSite;
	}

	public void setAuthCookieSameSite(String authCookieSameSite) {
		this.authCookieSameSite = authCookieSameSite;
	}

	public int getSonarWeekCount() {
		return this.sonarWeekCount;
	}

	public void setSonarWeekCount(int sonarWeekCount) {
		this.sonarWeekCount = sonarWeekCount;
	}

	public int getJenkinsWeekCount() {
		return jenkinsWeekCount;
	}

	public void setJenkinsWeekCount(int jenkinsWeekCount) {
		this.jenkinsWeekCount = jenkinsWeekCount;
	}

	/**
	 * get priority
	 *
	 * @return the priority
	 */
	public Map<String, List<String>> getPriority() {
		return priority;
	}

	/**
	 * set priority
	 *
	 * @param priority
	 *            to set
	 */
	public void setPriority(Map<String, List<String>> priority) {
		this.priority = priority;
	}

	public int getNumberOfPastWeeksForKanbanCapacity() {
		return numberOfPastWeeksForKanbanCapacity;
	}

	public void setNumberOfPastWeeksForKanbanCapacity(int numberOfPastWeeksForKanbanCapacity) {
		this.numberOfPastWeeksForKanbanCapacity = numberOfPastWeeksForKanbanCapacity;
	}

	public int getNumberOfFutureWeeksForKanbanCapacity() {
		return numberOfFutureWeeksForKanbanCapacity;
	}

	public void setNumberOfFutureWeeksForKanbanCapacity(int numberOfFutureWeeksForKanbanCapacity) {
		this.numberOfFutureWeeksForKanbanCapacity = numberOfFutureWeeksForKanbanCapacity;
	}

	public int getNumberOfPastDaysForKanbanTestExecution() {
		return numberOfPastDaysForKanbanTestExecution;
	}

	public void setNumberOfPastDaysForKanbanTestExecution(int numberOfPastDaysForKanbanTestExecution) {
		this.numberOfPastDaysForKanbanTestExecution = numberOfPastDaysForKanbanTestExecution;
	}

	public int getNumberOfFutureDaysForKanbanTestExecution() {
		return numberOfFutureDaysForKanbanTestExecution;
	}

	public void setNumberOfFutureDaysForKanbanTestExecution(int numberOfFutureDaysForKanbanTestExecution) {
		this.numberOfFutureDaysForKanbanTestExecution = numberOfFutureDaysForKanbanTestExecution;
	}

	public boolean isSsoLogin() {
		return ssoLogin;
	}

	public void setSsoLogin(boolean ssoLogin) {
		this.ssoLogin = ssoLogin;
	}

	public Map<String, String> getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(Map<String, String> mailTemplate) {
		this.mailTemplate = mailTemplate;
	}

	public boolean isMailWithoutKafka() {
		return mailWithoutKafka;
	}

	public void setMailWithoutKafka(boolean mailWithoutKafka) {
		this.mailWithoutKafka = mailWithoutKafka;
	}

	public String getHostPath() {
		return hostPath;
	}

	public void setHostPath(String hostPath) {
		this.hostPath = hostPath;

	}

	public String getSamlTokenStartString() {
		return samlTokenStartString;
	}

	public void setSamlTokenStartString(String samlTokenStartString) {
		this.samlTokenStartString = samlTokenStartString;
	}

	public String getSamlTokenEndString() {
		return samlTokenEndString;
	}

	public void setSamlTokenEndString(String samlTokenEndString) {
		this.samlTokenEndString = samlTokenEndString;
	}

	public String getSamlUrlStartString() {
		return samlUrlStartString;
	}

	public void setSamlUrlStartString(String samlUrlStartString) {
		this.samlUrlStartString = samlUrlStartString;
	}

	public String getSamlUrlEndString() {
		return samlUrlEndString;
	}

	public void setSamlUrlEndString(String samlUrlEndString) {
		this.samlUrlEndString = samlUrlEndString;
	}

	public int getSprintCountForBackLogStrength() {
		return sprintCountForBackLogStrength;
	}

	public Integer getRepoXAxisCountForCheckInsAndMergeRequests() {
		return repoXAxisCountForCheckInsAndMergeRequests;
	}

	public void setRepoXAxisCountForCheckInsAndMergeRequests(Integer repoXAxisCountForCheckInsAndMergeRequests) {
		this.repoXAxisCountForCheckInsAndMergeRequests = repoXAxisCountForCheckInsAndMergeRequests;
	}

	public int getFlowKpiMonthCount() {
		return flowKpiMonthCount;
	}

	public void setFlowKpiMonthCount(int flowKpiMonthCount) {
		this.flowKpiMonthCount = flowKpiMonthCount;
	}

	public Integer getSonarMonthCount() {
		return sonarMonthCount;
	}

	public void setSonarMonthCount(Integer sonarMonthCount) {
		this.sonarMonthCount = sonarMonthCount;
	}
}
