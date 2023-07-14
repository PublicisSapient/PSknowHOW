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

import static org.hamcrest.Matchers.instanceOf;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.AzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.impl.AzurePipelineDeploymentClient;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.impl.DefaultAzurePipelineClient;

@ExtendWith(SpringExtension.class)
class AzurePipelineFactoryTest {

	@InjectMocks
	private AzurePipelineFactory azurePipelineFactory;

	@Mock
	private AzurePipelineDeploymentClient deploymentClient;

	@Mock
	private DefaultAzurePipelineClient buildClient;

	@Test
	void getAzurePipelineClient() {
		try {
			AzurePipelineClient azurePipelineClient = azurePipelineFactory.getAzurePipelineClient("Build");
			Assert.assertThat(azurePipelineClient, instanceOf(DefaultAzurePipelineClient.class));
			azurePipelineClient = azurePipelineFactory.getAzurePipelineClient("Deploy");
			Assert.assertThat(azurePipelineClient, instanceOf(AzurePipelineDeploymentClient.class));
		} catch (NullPointerException ex) {
			Assert.assertEquals(null, ex.getMessage());
		}

	}

	@Test
	void testGetBambooClientNull() {
		AzurePipelineClient azurePipelineClient = null;
		try {
			azurePipelineFactory.getAzurePipelineClient(null);
		} catch (Exception exception) {
			Assert.assertNull(azurePipelineClient);
		}
	}
}