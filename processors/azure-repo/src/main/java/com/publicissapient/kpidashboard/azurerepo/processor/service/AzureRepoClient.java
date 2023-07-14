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

package com.publicissapient.kpidashboard.azurerepo.processor.service;

import java.util.List;

import com.publicissapient.kpidashboard.azurerepo.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.azurerepo.model.AzureRepoModel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;

/**
 * AzureRepoClient for getting all commits from AzureRepo
 */
public interface AzureRepoClient {

	/**
	 * Get all commits.
	 *
	 * 
	 * @param azurerepoRepo
	 *            azurerepoRepo
	 * @param firstTimeRun
	 *            firstTimeRun
	 * @param azureRepoProcessorInfo
	 *            azureRepoProcessorInfo
	 * @return list
	 * @throws FetchingCommitException
	 */
	List<CommitDetails> fetchAllCommits(AzureRepoModel azurerepoRepo, boolean firstTimeRun,
			ProcessorToolConnection azureRepoProcessorInfo, ProjectBasicConfig projectBasicConfig)
			throws FetchingCommitException;

	List<MergeRequests> fetchAllMergeRequest(AzureRepoModel azurerepoRepo, boolean firstTimeRun,
			ProcessorToolConnection azureRepoProcessorInfo, ProjectBasicConfig proBasicConfig)
			throws FetchingCommitException;

}
