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

package com.publicissapient.kpidashboard.azurepipeline.processor.adapter;

import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

/**
 * Client for fetching job and build information from AzurePipeline.
 */
public interface AzurePipelineClient {

	/**
	 * Finds all of the configured jobs for a given instance and returns the set of
	 * builds for each job. At a minimum, the number and url of each Build will be
	 * populated.
	 *
	 * @param azurePipelineServer
	 *            the URL for the AzurePipeline instance
	 * @param lastStartTimeOfJobs
	 *            lastStartTimeOfBuilds
	 * @param proBasicConfig
	 * @return a summary of every build for each job on the instance
	 */
	Map<ObjectId, Set<Build>> getInstanceJobs(ProcessorToolConnection azurePipelineServer, long lastStartTimeOfJobs,
			ProjectBasicConfig proBasicConfig);

	/**
	 * Finds all of the configured jobs for a given instance and returns the set of
	 * deployments for each job. At a minimum, the number and url of each Deployment
	 * will be populated.
	 *
	 * @param azurePipelineServer
	 *            the URL for the AzurePipeline instance
	 * @param lastStartTimeOfJobs
	 *            lastStartTimeOfDeployments
	 * @param proBasicConfig
	 * @return a summary of every deployment for each job on the instance
	 */

	Map<Deployment, Set<Deployment>> getDeploymentJobs(ProcessorToolConnection azurePipelineServer,
			long lastStartTimeOfJobs, ProjectBasicConfig proBasicConfig);

}
