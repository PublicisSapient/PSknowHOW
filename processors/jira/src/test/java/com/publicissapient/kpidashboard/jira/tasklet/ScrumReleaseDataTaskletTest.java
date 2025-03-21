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

package com.publicissapient.kpidashboard.jira.tasklet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.FetchScrumReleaseData;
import com.publicissapient.kpidashboard.jira.service.JiraClientService;

@RunWith(MockitoJUnitRunner.class)
public class ScrumReleaseDataTaskletTest {

	@Mock
	private FetchProjectConfiguration fetchProjectConfiguration;

	@Mock
	private JiraClientService jiraClientService;

	@Mock
	FetchScrumReleaseData fetchKanbanReleaseData;

	@Mock
	private StepContribution stepContribution;

	@Mock
	private ChunkContext chunkContext;

	@InjectMocks
	private ScrumReleaseDataTasklet jiraIssueReleaseStatusTasklet;

	@Before
	public void setUp() {
		// Mock any setup or common behavior needed before each test
	}

	@Test
	public void testExecute() throws Exception {
		// Arrange
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().projectName("KnowHow").build();

		when(fetchProjectConfiguration.fetchConfiguration(null)).thenReturn(projectConfFieldMapping);

		// Act
		RepeatStatus result = jiraIssueReleaseStatusTasklet.execute(stepContribution, chunkContext);

		// Assert
		verify(fetchKanbanReleaseData, times(1)).processReleaseInfo(projectConfFieldMapping, null);
		assertEquals(RepeatStatus.FINISHED, result);
	}
}
