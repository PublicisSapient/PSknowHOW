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

package com.publicissapient.kpidashboard.teamcity.processor.adapter;

import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

/**
 * Client for fetching job and build information from Teamcity.
 */
public interface TeamcityClient {

	/**
	 * Finds all of the configured jobs for a given instance and returns the set of
	 * builds for each job. At a minimum, the number and url of each Build will be
	 * populated.
	 *
	 * @param teamcityServer
	 *            the URL for the Teamcity instance
	 * @return a summary of every build for each job on the instance
	 */
	Map<ObjectId, Set<Build>> getInstanceJobs(ProcessorToolConnection teamcityServer);

	/**
	 * Fetch full populated build information for a build.
	 *
	 * @param buildUrl
	 *            the url of the build
	 * @param instanceUrl
	 *            the url of Teamcity server
	 * @param teamcityServer
	 *            the teamcity server
	 * @param proBasicConfig
	 * @return a Build instance or null
	 */
	Build getBuildDetails(String buildUrl, String instanceUrl, ProcessorToolConnection teamcityServer,
			ProjectBasicConfig proBasicConfig);
}
