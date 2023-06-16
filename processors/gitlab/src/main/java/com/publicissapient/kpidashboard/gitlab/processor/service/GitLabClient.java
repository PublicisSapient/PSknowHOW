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

package com.publicissapient.kpidashboard.gitlab.processor.service;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.gitlab.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.gitlab.model.GitLabInfo;
import com.publicissapient.kpidashboard.gitlab.model.GitLabRepo;

/**
 * GitLabClient for getting all commits from GitLab
 */
public interface GitLabClient {

	/**
	 * Get all commits.
	 *
	 * @param gitLabRepo
	 *            the gitLabRepo
	 * @param firstTimeRun
	 *            the first time run
	 * @param gitLabInfo
	 *            tool and connections info
	 * @return the list
	 * @throws FetchingCommitException
	 *             the exception
	 */
	List<CommitDetails> fetchAllCommits(GitLabRepo gitLabRepo, boolean firstTimeRun, GitLabInfo gitLabInfo)
			throws FetchingCommitException;

}
