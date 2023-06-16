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

package com.publicissapient.kpidashboard.github.processor.service;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.github.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.github.model.GitHubProcessorItem;

/**
 * GithubClient for getting all commits and merge requests from github
 */
public interface GitHubClient {

	/**
	 * Get all commits.
	 *
	 * @param gitHubProcessorItem
	 *            the gitHubProcessorItem
	 * @param firstTimeRun
	 *            the first time run
	 * @param processorToolConnection
	 *            ProcessorToolConnection like url,userId
	 * @return the list
	 * @throws FetchingCommitException
	 *             the exception
	 */
	List<CommitDetails> fetchAllCommits(GitHubProcessorItem gitHubProcessorItem, boolean firstTimeRun,
			ProcessorToolConnection processorToolConnection, ProjectBasicConfig proBasicConfig)
			throws FetchingCommitException;

	/**
	 * @param gitHubProcessorItem
	 *            the gitHubProcessorItem
	 * @param firstTimeRun
	 *            the first time run
	 * @param processorToolConnection
	 *            processorToolConnection like url,userId
	 * @return the list of merge request Detail
	 * @throws FetchingCommitException
	 *             the exception
	 */
	List<MergeRequests> fetchMergeRequests(GitHubProcessorItem gitHubProcessorItem, boolean firstTimeRun,
			ProcessorToolConnection processorToolConnection, ProjectBasicConfig proBasicConfig)
			throws FetchingCommitException;

}
