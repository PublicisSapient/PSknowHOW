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

package com.publicissapient.kpidashboard.azure.client.azureissue;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;

@ExtendWith(SpringExtension.class)
public class AzureIssueClientFactoryTest {

	@InjectMocks
	AzureIssueClientFactory azureIssueClientFactory;
	ProjectConfFieldMapping projectConfFieldMapping;
	@Mock
	private KanbanAzureIssueClientImpl kanbanAzureIssueClient;
	@Mock
	private ScrumAzureIssueClientImpl scrumAzureIssueClient;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getAzureIssueDataClientKanban() {
		prepareProjectConfig();
		Assert.assertEquals(kanbanAzureIssueClient,
				azureIssueClientFactory.getAzureIssueDataClient(projectConfFieldMapping));
	}

	@Test
	public void getAzureIssueDataClientScrum() {
		prepareProjectConfigScrum();
		Assert.assertEquals(scrumAzureIssueClient,
				azureIssueClientFactory.getAzureIssueDataClient(projectConfFieldMapping));
	}

	private void prepareProjectConfig() {
		projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		projectConfFieldMapping.setKanban(true);
	}

	private void prepareProjectConfigScrum() {
		projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		projectConfFieldMapping.setKanban(false);
	}

}