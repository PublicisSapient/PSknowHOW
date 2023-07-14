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

package com.publicissapient.kpidashboard.jenkins.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.jenkins.processor.adapter.JenkinsClient;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.impl.JenkinsBuildClient;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.impl.JenkinsDeployClient;

/**
 * Provides factory to create Jenkins Clients.
 *
 */
@Component
public class JenkinsClientFactory {

	private final String build = "build";
	private final String deploy = "deploy";
	private final JenkinsBuildClient buildClient;
	private final JenkinsDeployClient deployClient;

	@Autowired
	public JenkinsClientFactory(JenkinsBuildClient buildClient, JenkinsDeployClient deployClient) {
		this.buildClient = buildClient;
		this.deployClient = deployClient;
	}

	/**
	 * Provides instance of Jenkins client.
	 * 
	 * @param jobType
	 * @return returns the instance of Jenkins Client
	 */
	public JenkinsClient getJenkinsClient(String jobType) {
		JenkinsClient jenkinsClient = null;
		if (jobType.equalsIgnoreCase(build)) {
			jenkinsClient = buildClient;
		} else if (jobType.equalsIgnoreCase(deploy)) {
			jenkinsClient = deployClient;
		}
		return jenkinsClient;
	}

}
