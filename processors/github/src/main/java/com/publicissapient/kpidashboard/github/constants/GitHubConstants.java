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

package com.publicissapient.kpidashboard.github.constants;

/**
 * BitBucketConstants represents a class which holds BitBucketConfiuration
 * related fields
 */
public final class GitHubConstants {

	/** The Constant SCM. */
	public static final String SCM = "scm";

	/** The Constant URL. */
	public static final String URL = "url";

	/** The Constant TOOL_BRANCH. */
	public static final String TOOL_BRANCH = "branch";

	/** The Constant RESP_AUTHOR_KEY. */
	public static final String RESP_AUTHOR_KEY = "author";

	/** The Constant RESP_MESSAGE_KEY. */
	public static final String RESP_MESSAGE_KEY = "message";

	/** The Constant RESP_PARENTS_KEY. */
	public static final String RESP_PARENTS_KEY = "parents";

	/** The Constant RESP_ID_KEY. */
	public static final String RESP_ID_KEY = "sha";

	/** The Constant RESP_NAME_KEY. */
	public static final String RESP_NAME_KEY = "name";

	/** The Constant RESP_AUTHOR_TIMESTAMP_KEY. */
	public static final String RESP_AUTHOR_TIMESTAMP_KEY = "date";

	public static final String PER_PAGE_SIZE = "100";

	/** The Constant URL. */
	public static final String REPO_NAME = "repoName";

	/** The Constant REPO_BRANCH. */
	public static final String REPO_BRANCH = "branch";

	/** MergeRequests Constants */
	public static final String RESP_TITLE = "title";
	public static final String RESP_STATE = "state";
	public static final String RESP_OPEN = "open";
	public static final String RESP_CLOSED = "closed";
	public static final String RESP_CREATED_AT = "created_at";
	public static final String RESP_UPDATED_AT = "updated_at";
	public static final String RESP_CLOSED_AT = "closed_at";
	public static final String OWNER = "owner";
	public static final String MERGED = "MERGED";
	public static final String RESP_COMMIT = "commit";
	public static final String RESP_MERGED_AT = "merged_at";
	public static final String RESP_HEAD = "head";
	public static final String RESP_BASE = "base";
	public static final String RESP_REF = "ref";
	public static final String RESP_USER = "user";
	public static final String RESP_LOGIN = "login";
	public static final String RESP_NUMBER = "number";
	public static final String RESP_REQUESTED_REVIEWERS = "requested_reviewers";
	public static final String RESP_ID = "id";

	/**
	 * Instantiates a new bit bucket constants.
	 */
	private GitHubConstants() {
	}
}
