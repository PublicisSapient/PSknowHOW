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

package com.publicissapient.kpidashboard.azurepipeline.processor.adapter.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
class AzurePipelineDeploymentClientTest {

	private static final ProcessorToolConnection azurePipelineServer = new ProcessorToolConnection();
	private static final ProjectBasicConfig proBasicConfig = new ProjectBasicConfig();
	@Mock
	private RestOperationsFactory<RestOperations> restOperationsFactory;
	@InjectMocks
	private AzurePipelineDeploymentClient azurePipelineDeploymentClient;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();

	@BeforeEach
	public void init() {
		azurePipelineServer.setUrl("https://test.com/testUser/testProject");
		azurePipelineServer.setUsername(null);
		azurePipelineServer.setJobName("1");
		azurePipelineServer.setJobType("Deploy");
		azurePipelineServer.setBasicProjectConfigId(new ObjectId("629f47946000f87b2c5050b5"));
		azurePipelineServer.setId(new ObjectId("629f47946000f87b2c5050b3"));
		azurePipelineServer.setId(new ObjectId("629f47946000f87b2c5050b4"));
		azurePipelineServer.setApiVersion("6.0");

		proBasicConfig.setId(new ObjectId("629f47946000f87b2c5050b5"));
		proBasicConfig.setSaveAssigneeDetails(true);
		projectConfigList.add(proBasicConfig);

	}

	@Test
	void testFetchDeployJobs() throws Exception {
		try {
			HttpHeaders header = new HttpHeaders();
			header.add("Authorization", "base64str");
			when(restOperationsFactory.getTypeInstance().exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET),
					ArgumentMatchers.any(HttpEntity.class), eq(String.class))).thenReturn(
							new ResponseEntity<>(getServerResponseFromJson("deployments.json"), HttpStatus.OK));
			Map<Deployment, Set<Deployment>> response = azurePipelineDeploymentClient
					.getDeploymentJobs(azurePipelineServer, 0, proBasicConfig);
			assertEquals(1, response.size());
			Deployment deployment = response.values().stream().iterator().next().stream().iterator().next();
			assertEquals("knowhow-release", deployment.getJobName());
			assertEquals("QA server release", deployment.getEnvName());
			assertEquals(DeploymentStatus.SUCCESS, deployment.getDeploymentStatus());
			assertEquals("24", deployment.getNumber());
		} catch (Exception ex) {
			log.error("Exception occurred" + ex);
		}
	}

	private String getServerResponseFromJson(String fileName) throws IOException {
		String filePath = "src/test/resources/com/publicissapient/kpidashboard/processor/" + fileName;
		return new String(Files.readAllBytes(Paths.get(filePath)));

	}
}