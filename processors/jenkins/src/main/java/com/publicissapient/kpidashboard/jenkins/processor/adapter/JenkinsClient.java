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

package com.publicissapient.kpidashboard.jenkins.processor.adapter;

import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.jenkins.model.JenkinsProcessor;

/**
 * Client for fetching job and build information from Jenkins.
 */
public interface JenkinsClient {

	/**
	 * Finds all details for a given jub and returns the set of builds for the job.
	 * At a minimum, the number and url of each Build will be populated.
	 *
	 * @param jenkinsServer
	 *            the URL for the Jenkins instance
	 * @param proBasicConfig
	 * @return a summary of every build for each job on the instance
	 */
	Map<ObjectId, Set<Build>> getBuildJobsFromServer(ProcessorToolConnection jenkinsServer,
			ProjectBasicConfig proBasicConfig);

	Map<String, Set<Deployment>> getDeployJobsFromServer(ProcessorToolConnection jenkinsServer,
			JenkinsProcessor processor);
}
