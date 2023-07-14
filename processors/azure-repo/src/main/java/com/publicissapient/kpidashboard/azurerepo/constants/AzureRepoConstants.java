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

package com.publicissapient.kpidashboard.azurerepo.constants;

/**
 * AzureRepoConstants represents a class which holds AzureRepoConfiuration
 * related fields.
 */
public final class AzureRepoConstants {

	/** The Constant SCM. */
	public static final String SCM = "scm";

	/** The Constant URL. */
	public static final String URL = "url";

	/** The Constant TOOL_AZUREREPO. */
	public static final String TOOL_AZUREREPO = "AzureRepository";

	/** The Constant TOOL_BRANCH. */
	public static final String TOOL_BRANCH = "branch";

	/** The Constant REPOSITORY_NAME. */
	public static final String REPOSITORY_NAME = "repositoryName";

	/** The Constant RESP_VALUES_KEY. */
	public static final String RESP_VALUES_KEY = "value";

	/** The Constant RESP_AUTHOR_KEY. */
	public static final String RESP_AUTHOR_KEY = "author";

	/** The Constant RESP_MESSAGE_KEY. */
	public static final String RESP_MESSAGE_KEY = "comment";

	/** The Constant RESP_ID_KEY. */
	public static final String RESP_ID_KEY = "id";

	public static final String COMMIT_ID = "commitId";

	/** The Constant RESP_NAME_KEY. */
	public static final String RESP_NAME_KEY = "name";

	/** The Constant RESP_AUTHOR_TIMESTAMP_KEY. */
	public static final String RESP_AUTHOR_COMMITTER = "committer";

	public static final String RESP_AUTHOR_DATE = "date";

	/** The Constant BASIC_AUTH_PREFIX. */
	public static final String BASIC_AUTH_PREFIX = "Basic ";

	/** The Constant HTTP_AUTHORIZATION_HEADER. */
	public static final String HTTP_AUTHORIZATION_HEADER = "Authorization";

	/** The Constant API_VERSION. */
	public static final String API_VERSION = "apiVersion";

	/** The Constant count value. */
	public static final String COUNT = "count";

	/** MergeRequests Constants */
	public static final String RESP_TITLE = "title";
	public static final String RESP_STATUS = "status";
	public static final String RESP_OPEN = "open";
	public static final String RESP_CLOSED = "closed";
	public static final String RESP_CREATION_DATE = "creationDate";
	public static final String RESP_UPDATED_DATE = "updatedDate";
	public static final String RESP_CLOSED_DATE = "closedDate";
	public static final String RESP_REVIEWERS = "reviewers";
	public static final String RESP_SOURCE_REF_NAME = "sourceRefName";
	public static final String RESP_TARGET_REF_NAME = "targetRefName";
	public static final String RESP_DISP_NAME = "displayName";
	public static final String RESP_REPO = "repository";
	public static final String RESP_NAME = "name";
	public static final String RESP_PROJ = "project";
	public static final String RESP_CREATED_BY = "createdBy";

	/**
	 * Instantiates a new bit bucket constants .
	 */
	private AzureRepoConstants() {
	}
}
