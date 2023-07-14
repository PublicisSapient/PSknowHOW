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

package com.publicissapient.kpidashboard.github.model;

import java.util.Date;

import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;

/**
 * GitHubProcessorItem represents a class which contains github information
 * 
 * @see ProcessorItem
 */
public class GitHubProcessorItem extends ProcessorItem {

	/** The Constant USERID. */
	private static final String USERID = "userID";

	/** The Constant PASSWORD. */
	private static final String PASSWORD = "password";

	/** The Constant URL. */
	private static final String URL = "url";

	/** The Constant URL. */
	private static final String REPO_NAME = "repoName";

	/** The Constant REPO_BRANCH. */
	private static final String REPO_BRANCH = "branch";

	/** The Constant LAST_UPDATE_COMMIT. */
	private static final String LAST_UPDATED_COMMIT = "lastUpdatedCommit";

	/** The Constant LAST_UPDATE_TIME. */
	private static final String LAST_UPDATED_TIME = "lastUpdatedTime";

	/** The Constant GitLabAccessToken */
	private static final String GIT_LAB_ACCESS_TOKEN = "GitLabAccessToken";

	private static final String GIT_LAB_PROJECT_ID = "gitLabProjectId";

	private static final String GITLAB_LAST_COMMIT_TIMESTAMP = "commitTimestamp";

	private static final String OWNER = "owner";

	/**
	 * Gets the user id.
	 *
	 * @return userId from Options
	 */
	public String getUserId() {
		return (String) getToolDetailsMap().get(USERID);
	}

	/**
	 * Sets userId.
	 *
	 * @param userId
	 *            the new user id
	 */
	public void setUserId(String userId) {
		getToolDetailsMap().put(USERID, userId);
	}

	/**
	 * Gets the repo url.
	 *
	 * @return repoUrl from Options
	 */
	public String getRepoUrl() {
		return (String) getToolDetailsMap().get(URL);
	}

	/**
	 * Sets repoUrl.
	 *
	 * @param instanceUrl
	 *            the new repo url
	 */
	public void setRepoUrl(String instanceUrl) {
		getToolDetailsMap().put(URL, instanceUrl);
	}

	/**
	 * Gets the password.
	 *
	 * @return password from options
	 */
	public String getPassword() {
		return (String) getToolDetailsMap().get(PASSWORD);
	}

	/**
	 * Sets password.
	 *
	 * @param password
	 *            the new password
	 */
	public void setPassword(String password) {
		getToolDetailsMap().put(PASSWORD, password);
	}

	/**
	 * Gets the branch.
	 *
	 * @return branch from Options
	 */
	public String getBranch() {
		return (String) getToolDetailsMap().get(REPO_BRANCH);
	}

	/**
	 * Sets branch.
	 *
	 * @param branch
	 *            the new branch
	 */
	public void setBranch(String branch) {
		getToolDetailsMap().put(REPO_BRANCH, branch);
	}

	/**
	 * Gets the last update time.
	 *
	 * @return lastUpdateTime from Options
	 */
	public Date getLastUpdatedTime() {

		return (Date) getToolDetailsMap().get(LAST_UPDATED_TIME);
	}

	/**
	 * Sets lastUpdateTime.
	 *
	 * @param date
	 *            the new last update time
	 */
	public void setLastUpdatedTime(Date date) {
		getToolDetailsMap().put(LAST_UPDATED_TIME, date);
	}

	/**
	 * Gets the last update commitDetails.
	 *
	 * @return lastUpdateCommit
	 */
	public String getLastUpdatedCommit() {
		return (String) getToolDetailsMap().get(LAST_UPDATED_COMMIT);
	}

	/**
	 * Sets lastUpdateCommit.
	 *
	 * @param sha
	 *            the new last update commit
	 */
	public void setLastUpdatedCommit(String sha) {
		getToolDetailsMap().put(LAST_UPDATED_COMMIT, sha);
	}

	/**
	 * Gets the last GitLabAccessToken.
	 *
	 * @return gitLabAccessToken
	 */
	public String getGitLabAccessToken() {
		return (String) getToolDetailsMap().get(GIT_LAB_ACCESS_TOKEN);
	}

	/**
	 * Sets GitLabAccessToken.
	 *
	 * @param gitLabAccessToken
	 *            the new last update commit
	 */
	public void setGitLabAccessToken(String gitLabAccessToken) {
		getToolDetailsMap().put(GIT_LAB_ACCESS_TOKEN, gitLabAccessToken);
	}

	/**
	 * 
	 * @return gitlab project id
	 */
	public String getGitLabProjectId() {
		return (String) getToolDetailsMap().get(GIT_LAB_PROJECT_ID);
	}

	/**
	 * Set gitlab project id
	 * 
	 * @param gitLabProjectId
	 *            gitLabProjectId
	 */
	public void setGitLabProjectId(String gitLabProjectId) {
		getToolDetailsMap().put(GIT_LAB_PROJECT_ID, gitLabProjectId);
	}

	/**
	 * @return last commit timestamp
	 */
	public String getLastCommitTimestamp() {
		return (String) getToolDetailsMap().get(GITLAB_LAST_COMMIT_TIMESTAMP);
	}

	/**
	 * This method set last commited timestamp
	 * 
	 * @param lastCommitTimestamp
	 *            lastCommitTimestamp
	 */
	public void setLastCommitTimestamp(String lastCommitTimestamp) {
		getToolDetailsMap().put(GITLAB_LAST_COMMIT_TIMESTAMP, lastCommitTimestamp);
	}

	/**
	 * @return repo name
	 */
	public String getRepoName() {
		return (String) getToolDetailsMap().get(REPO_NAME);
	}

	/**
	 * set repo name
	 * 
	 * @param repoName
	 *            repoName
	 */
	public void setRepoName(String repoName) {
		getToolDetailsMap().put(REPO_NAME, repoName);
	}

	/**
	 * 
	 * @return ownername
	 */
	public String getOwner() {
		return (String) getToolDetailsMap().get(OWNER);
	}

	/**
	 * set owner name
	 * 
	 * @param owner
	 *            owner
	 */
	public void setOwner(String owner) {
		getToolDetailsMap().put(OWNER, owner);
	}

}
