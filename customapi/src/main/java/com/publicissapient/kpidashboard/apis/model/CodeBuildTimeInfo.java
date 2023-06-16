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

package com.publicissapient.kpidashboard.apis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to hold the build info lists.
 *
 * @author anisingh4
 */
public class CodeBuildTimeInfo {

	private List<String> buildJobList;
	private List<String> buildUrlList;
	private List<String> buildStartTimeList;
	private List<String> buildEndTimeList;
	private List<String> durationList;
	private List<String> buildStatusList;
	private List<String> startedByList;
	private List<String> weeksList;

	/**
	 * Instantiates a new Code build time info holder.
	 */
	public CodeBuildTimeInfo() {
		buildJobList = new ArrayList<>();
		buildUrlList = new ArrayList<>();
		buildStartTimeList = new ArrayList<>();
		buildEndTimeList = new ArrayList<>();
		durationList = new ArrayList<>();
		buildStatusList = new ArrayList<>();
		startedByList = new ArrayList<>();
		weeksList = new ArrayList<>();
	}

	/**
	 * Add buid job.
	 *
	 * @param buildJob
	 *            the build job
	 */
	public void addBuidJob(String buildJob) {
		buildJobList.add(buildJob);
	}

	/**
	 * Add buid url.
	 *
	 * @param buildUrl
	 *            the build url
	 */
	public void addBuildUrl(String buildUrl) {
		buildUrlList.add(buildUrl);
	}

	/**
	 * Add buidbuild start time.
	 *
	 * @param startTime
	 *            the start time
	 */
	public void addBuildStartTime(String startTime) {
		buildStartTimeList.add(startTime);
	}

	/**
	 * Addbuild end time.
	 *
	 * @param endTime
	 *            the end time
	 */
	public void addBuildEndTime(String endTime) {
		buildEndTimeList.add(endTime);
	}

	/**
	 * Add duration.
	 *
	 * @param duration
	 *            the duration
	 */
	public void addDuration(String duration) {
		durationList.add(duration);
	}

	/**
	 * Add build status.
	 *
	 * @param buildStatus
	 *            the build status
	 */
	public void addBuildStatus(String buildStatus) {
		buildStatusList.add(buildStatus);
	}

	/**
	 * Add started by.
	 *
	 * @param startedBy
	 *            the started by
	 */
	public void addStartedBy(String startedBy) {
		startedByList.add(startedBy);
	}

	/**
	 * Gets build job list.
	 *
	 * @return the build job list
	 */
	public List<String> getBuildJobList() {
		return buildJobList;
	}

	/**
	 * Sets build job list.
	 *
	 * @param buildJobList
	 *            the build job list
	 */
	public void setBuildJobList(List<String> buildJobList) {
		this.buildJobList = buildJobList;
	}

	/**
	 * Gets build url list.
	 *
	 * @return the build url list
	 */
	public List<String> getBuildUrlList() {
		return buildUrlList;
	}

	/**
	 * Sets build url list.
	 *
	 * @param buildUrlList
	 *            the build url list
	 */
	public void setBuildUrlList(List<String> buildUrlList) {
		this.buildUrlList = buildUrlList;
	}

	/**
	 * Gets build start time list.
	 *
	 * @return the build start time list
	 */
	public List<String> getBuildStartTimeList() {
		return buildStartTimeList;
	}

	/**
	 * Sets build start time list.
	 *
	 * @param buildStartTimeList
	 *            the build start time list
	 */
	public void setBuildStartTimeList(List<String> buildStartTimeList) {
		this.buildStartTimeList = buildStartTimeList;
	}

	/**
	 * Gets build end time list.
	 *
	 * @return the build end time list
	 */
	public List<String> getBuildEndTimeList() {
		return buildEndTimeList;
	}

	/**
	 * Sets build end time list.
	 *
	 * @param buildEndTimeList
	 *            the build end time list
	 */
	public void setBuildEndTimeList(List<String> buildEndTimeList) {
		this.buildEndTimeList = buildEndTimeList;
	}

	/**
	 * Gets duration list.
	 *
	 * @return the duration list
	 */
	public List<String> getDurationList() {
		return durationList;
	}

	/**
	 * Sets duration list.
	 *
	 * @param durationList
	 *            the duration list
	 */
	public void setDurationList(List<String> durationList) {
		this.durationList = durationList;
	}

	/**
	 * Gets build status list.
	 *
	 * @return the build status list
	 */
	public List<String> getBuildStatusList() {
		return buildStatusList;
	}

	/**
	 * Sets build status list.
	 *
	 * @param buildStatusList
	 *            the build status list
	 */
	public void setBuildStatusList(List<String> buildStatusList) {
		this.buildStatusList = buildStatusList;
	}

	/**
	 * Gets started by list.
	 *
	 * @return the started by list
	 */
	public List<String> getStartedByList() {
		return startedByList;
	}

	/**
	 * Sets started by list.
	 *
	 * @param startedByList
	 *            the started by list
	 */
	public void setStartedByList(List<String> startedByList) {
		this.startedByList = startedByList;
	}

	/**
	 * 
	 * @return week list
	 */
	public List<String> getWeeksList() {
		return weeksList;
	}

	/**
	 * set week list
	 * 
	 * @param weeksList
	 *            weeksList
	 */
	public void setWeeksList(List<String> weeksList) {
		this.weeksList = weeksList;
	}

	public void addWeeks(String week) {
		weeksList.add(week);
	}
}
