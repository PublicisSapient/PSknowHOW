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

package com.publicissapient.kpidashboard.gitlab.constants;

/**
 * GitLabConstants represents a class which holds GitLabConfiuration related
 * fields
 */
public final class GitLabConstants {

	/** The Constant SCM. */
	public static final String SCM = "scm";

	/** The Constant URL. */
	public static final String URL = "url";

	/** The Constant TOOL_BITBUCKET. */
	public static final String TOOL_GITLAB = "GitLab";

	/** The Constant TOOL_BRANCH. */
	public static final String TOOL_BRANCH = "branch";

	/** The Constant RESP_AUTHOR_KEY. */
	public static final String RESP_AUTHOR_KEY = "author";

	/** The Constant RESP_MESSAGE_KEY. */
	public static final String RESP_MESSAGE_KEY = "message";

	/** The Constant RESP_PARENTS_KEY. */
	public static final String RESP_PARENTS_KEY = "parent_ids";

	/** The Constant RESP_ID_KEY. */
	public static final String RESP_ID_KEY = "id";

	/** The Constant RESP_NAME_KEY. */
	public static final String RESP_NAME_KEY = "author_name";

	/** The Constant RESP_AUTHOR_TIMESTAMP_KEY. */
	public static final String RESP_AUTHOR_TIMESTAMP_KEY = "authored_date";

	/** The Constant HTTP_AUTHORIZATION_HEADER. */
	public static final String HTTP_AUTHORIZATION_HEADER = "Authorization";

	/** The GITLAB Access Token. */
	public static final String GITLAB_ACCESS_TOKEN = "GitLabAccessToken";

	/** The GitLab Private Token. */
	public static final String PRIVATE_TOKEN = "PRIVATE-TOKEN";

	public static final String GITLAB_API = "/api/v4/projects";

	public static final String GIT_LAB_PROJECT_ID = "gitLabProjectId";

	public static final String GITLAB_URL_API_REPO = "/repository";

	public static final String GITLAB_URL_API_MERGEREQUEST = "/merge_requests";

	public static final String GITLAB_URL_API_COMMIT = "/commits";

	public static final String PER_PAGE_SIZE = "100";

	/** MergeRequests Constants */
	public static final String RESP_TITLE = "title";
	public static final String RESP_STATE = "state";
	public static final String RESP_OPEN = "open";
	public static final String RESP_CLOSED = "closed";
	public static final String RESP_CREATED_AT = "created_at";
	public static final String RESP_UPDATED_AT = "updated_at";
	public static final String RESP_CLOSED_AT = "closed_at";
	public static final String RESP_USER_NAME = "username";
	public static final String RESP_REVIEWERS = "reviewers";
	public static final String RESP_SOURCE_BRANCH = "source_branch";
	public static final String RESP_TARGET_BRANCH = "target_branch";
	public static final String RESP_PROJECT_ID = "project_id";

	/**
	 * Instantiates a new bit bucket constants.
	 */
	private GitLabConstants() {
	}
}
