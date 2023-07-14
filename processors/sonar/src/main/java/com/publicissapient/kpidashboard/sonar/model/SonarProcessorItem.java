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

package com.publicissapient.kpidashboard.sonar.model;

import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;

/**
 * Provides Sonar Project setup properties.
 */
public class SonarProcessorItem extends ProcessorItem {
	private static final String INSTANCE_URL = "instanceUrl";
	private static final String PROJECT_NAME = "projectName";
	private static final String PROJECT_ID = "projectId";
	private static final String KEY = "key";
	private static final String BRANCH = "branch";
	private static final String LATEST_VERSION = "latestVersion";
	private static final String TIMESTAMP = "timestamp";

	/**
	 * Provides instance URL.
	 * 
	 * @return the instance url from options
	 */
	public String getInstanceUrl() {
		return (String) getToolDetailsMap().get(INSTANCE_URL);
	}

	/**
	 * Sets instance URL.
	 * 
	 * @param instanceUrl
	 *            the instance URL
	 */
	public void setInstanceUrl(String instanceUrl) {
		getToolDetailsMap().put(INSTANCE_URL, instanceUrl);
	}

	/**
	 * Provides Project Id.
	 * 
	 * @return the projectId from Options
	 */
	public String getProjectId() {
		return (String) getToolDetailsMap().get(PROJECT_ID);
	}

	/**
	 * Sets Project Id.
	 * 
	 * @param id
	 *            the project ID
	 */
	public void setProjectId(String id) {
		getToolDetailsMap().put(PROJECT_ID, id);
	}

	/**
	 * Provides Project Name.
	 * 
	 * @return the project name from Options
	 */
	public String getProjectName() {
		return (String) getToolDetailsMap().get(PROJECT_NAME);
	}

	/**
	 * Sets Project name.
	 * 
	 * @param name
	 *            the project name
	 */
	public void setProjectName(String name) {
		getToolDetailsMap().put(PROJECT_NAME, name);
	}

	/**
	 * Get Project key.
	 * 
	 * @return key the project key
	 */
	public String getKey() {
		return (String) getToolDetailsMap().get(KEY);
	}

	/**
	 * Sets Project key.
	 * 
	 * @param key
	 *            the project key
	 */
	public void setKey(String key) {
		getToolDetailsMap().put(KEY, key);
	}

	/**
	 * Provides latest version.
	 * 
	 * @return the latest version
	 */
	public String getLatestVersion() {
		return (String) getToolDetailsMap().get(LATEST_VERSION);
	}

	/**
	 * Sets latest version.
	 * 
	 * @param key
	 *            the project key
	 */
	public void setLatestVersion(String key) {
		getToolDetailsMap().put(LATEST_VERSION, key);
	}

	/**
	 * Provides Timestamp.
	 * 
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return (long) getToolDetailsMap().get(TIMESTAMP);
	}

	/**
	 * Sets Timestamp.
	 * 
	 * @param timestamp
	 *            the timestamp
	 */
	public void setTimestamp(Long timestamp) {
		getToolDetailsMap().put(TIMESTAMP, timestamp);
	}

	/**
	 * Provides branch.
	 * 
	 * @return branch
	 */
	public String getBranch() {
		return (String) getToolDetailsMap().get(BRANCH);
	}

	/**
	 * Sets branch.
	 * 
	 * @param branch
	 */
	public void setBranch(String branch) {
		getToolDetailsMap().put(BRANCH, branch);
	}

	/**
	 * Overridden method of Object's equals method.
	 * 
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		SonarProcessorItem that = (SonarProcessorItem) obj;
		return getKey().equals(that.getKey()) && getInstanceUrl().equals(that.getInstanceUrl())
				&& getToolConfigId().toString().equals(that.getToolConfigId().toString());
	}

	/**
	 * Overridden method of Object's hashCode method.
	 * 
	 * @return hashcode
	 */
	@Override
	public int hashCode() {
		int result = getInstanceUrl().hashCode();
		result = 31 * result + getKey().hashCode();
		return result;
	}

	/**
	 * Provides Sonar Project properties details.
	 */
	@Override
	public String toString() {
		return "SonarProject [getInstanceUrl()=" + getInstanceUrl() + ", getProjectName()=" + getProjectName()
				+ ", getKey()=" + getKey() + ", getLatestVersion()=" + getLatestVersion() + ", getTimestamp()="
				+ getTimestamp() + ", hashCode()=" + hashCode() + "]";
	}
}
