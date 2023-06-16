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

package com.publicissapient.kpidashboard.azurepipeline.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.AzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.impl.AzurePipelineDeploymentClient;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.impl.DefaultAzurePipelineClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AzurePipelineFactory {

	private final DefaultAzurePipelineClient azurePipelineBuildClient;
	private final AzurePipelineDeploymentClient azurePipelineDeploymentClient;

	/**
	 * Instantiate AzurePipelineFactory .
	 *
	 * @param azurePipelineBuildClient
	 *            Azure Pipeline Build Client
	 * @param azurePipelineDeploymentClient
	 *            Azure Pipeline Deployment Client
	 * 
	 */
	@Autowired
	public AzurePipelineFactory(DefaultAzurePipelineClient azurePipelineBuildClient,
			AzurePipelineDeploymentClient azurePipelineDeploymentClient) {

		this.azurePipelineBuildClient = azurePipelineBuildClient;
		this.azurePipelineDeploymentClient = azurePipelineDeploymentClient;
	}

	/**
	 *
	 * @param jobType
	 * @return
	 */
	public AzurePipelineClient getAzurePipelineClient(String jobType) {

		AzurePipelineClient azurePipelineClient = null;
		if (jobType.equalsIgnoreCase("Build")) {
			azurePipelineClient = azurePipelineBuildClient;

		} else if (jobType.equalsIgnoreCase("Deploy")) {
			azurePipelineClient = azurePipelineDeploymentClient;
		}

		return azurePipelineClient;
	}

}
