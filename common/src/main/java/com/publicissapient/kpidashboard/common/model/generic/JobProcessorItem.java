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

package com.publicissapient.kpidashboard.common.model.generic;

/**
 * The type Job collector item.
 */
public class JobProcessorItem extends ProcessorItem {
	/**
	 * The constant INSTANCE_URL.
	 */
	public static final String INSTANCE_URL = "instanceUrl";
	/**
	 * The constant JOB_NAME.
	 */
	public static final String JOB_NAME = "jobName";
	/**
	 * The constant JOB_URL.
	 */
	protected static final String JOB_URL = "jobUrl";

	/**
	 * Gets instance url.
	 *
	 * @return the instance url
	 */
	public String getInstanceUrl() {
		return (String) getToolDetailsMap().get(INSTANCE_URL);
	}

	/**
	 * Sets instance url.
	 *
	 * @param instanceUrl
	 *            the instance url
	 */
	public void setInstanceUrl(String instanceUrl) {
		getToolDetailsMap().put(INSTANCE_URL, instanceUrl);
	}

	/**
	 * Gets job name.
	 *
	 * @return the job name
	 */
	public String getJobName() {
		return (String) getToolDetailsMap().get(JOB_NAME);
	}

	/**
	 * Sets job name.
	 *
	 * @param jobName
	 *            the job name
	 */
	public void setJobName(String jobName) {
		getToolDetailsMap().put(JOB_NAME, jobName);
	}

	/**
	 * Gets job url.
	 *
	 * @return the job url
	 */
	public String getJobUrl() {
		return (String) getToolDetailsMap().get(JOB_URL);
	}

	/**
	 * Sets job url.
	 *
	 * @param jobUrl
	 *            the job url
	 */
	public void setJobUrl(String jobUrl) {
		getToolDetailsMap().put(JOB_URL, jobUrl);
	}
}
