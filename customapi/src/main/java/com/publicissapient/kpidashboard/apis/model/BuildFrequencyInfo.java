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
 * This class is used to hold the build frequency info lists.
 * 
 * @author aksshriv1
 */
public class BuildFrequencyInfo {

	private List<String> buildJobNameList;
	private List<String> buildUrlList;
	private List<String> buildStartDate;
	private List<String> weeksList;

	public BuildFrequencyInfo() {
		buildJobNameList = new ArrayList<>();
		buildUrlList = new ArrayList<>();
		buildStartDate = new ArrayList<>();
		weeksList = new ArrayList<>();
	}

	/**
	 * add build job name
	 * 
	 * @param jobName
	 */
	public void addBuildJobNameList(String jobName) {
		buildJobNameList.add(jobName);
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
		buildStartDate.add(startTime);
	}

	/**
	 * Gets build job list.
	 *
	 * @return the build job list
	 */
	public List<String> getBuildJobList() {
		return buildJobNameList;
	}

	/**
	 * Sets build job list.
	 *
	 * @param buildJobNameList
	 *            the build job list
	 */
	public void setBuildJobList(List<String> buildJobNameList) {
		this.buildJobNameList = buildJobNameList;
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
		return buildStartDate;
	}

	/**
	 * Sets build start time list.
	 *
	 * @param buildStartDate
	 *            the build start time list
	 */
	public void setBuildStartTimeList(List<String> buildStartDate) {
		this.buildStartDate = buildStartDate;
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

	/**
	 * add weeks
	 * 
	 * @param week
	 */
	public void addWeeks(String week) {
		weeksList.add(week);
	}

}
