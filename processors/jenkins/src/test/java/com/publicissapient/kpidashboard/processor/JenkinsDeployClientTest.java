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

package com.publicissapient.kpidashboard.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.jenkins.model.JenkinsProcessor;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.impl.JenkinsDeployClient;

@ExtendWith(SpringExtension.class)
public class JenkinsDeployClientTest {

	private static final ProcessorToolConnection JENKINS_SERVER = new ProcessorToolConnection();
	private static final JenkinsProcessor Jenkins_Processor = new JenkinsProcessor();
	@Mock
	private RestOperationsFactory<RestOperations> restOperationsFactory;
	@Mock
	private RestOperations restOperations;
	private JenkinsDeployClient jenkinsDeployClient;

	@BeforeEach
	public void init() {
		when(restOperationsFactory.getTypeInstance()).thenReturn(restOperations);
		jenkinsDeployClient = new JenkinsDeployClient(restOperationsFactory);
		JENKINS_SERVER.setUrl("http://does:matter@jenkins.com/");
		JENKINS_SERVER.setUsername("does");
		JENKINS_SERVER.setApiKey("matter");
		JENKINS_SERVER.setJobName("deployJob");
		JENKINS_SERVER.setParameterNameForEnvironment("SERVER_IP");
		JENKINS_SERVER.setBasicProjectConfigId(new ObjectId("629da26040829b77c61658d4"));
		JENKINS_SERVER.setId(new ObjectId("629da26040829b77c61658d5"));
		Jenkins_Processor.setId(new ObjectId("629da26040829b77c61658d6"));
	}

	@Test
	void testGetDeployJobsFromServer() throws Exception {
		when(restOperations.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("deployments.json"), HttpStatus.OK));
		Map<String, Set<Deployment>> response = jenkinsDeployClient.getDeployJobsFromServer(JENKINS_SERVER,
				Jenkins_Processor);
		assertEquals(1, response.size());
		Deployment deployment = response.values().stream().iterator().next().stream().iterator().next();
		assertEquals("deployJob", deployment.getJobName());
		assertEquals("Test", deployment.getEnvName());
		assertEquals(DeploymentStatus.SUCCESS, deployment.getDeploymentStatus());
		assertEquals("1737", deployment.getNumber());
	}

	private String getJson(String fileName) throws IOException {
		InputStream inputStream = JenkinsDeployClientTest.class.getResourceAsStream(fileName);
		return IOUtils.toString(inputStream);
	}
}
