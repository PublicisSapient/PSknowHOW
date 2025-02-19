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
package com.publicissapient.kpidashboard.jira.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;

@RunWith(MockitoJUnitRunner.class)
public class JiraClientServiceTest {

	@Mock
	ProcessorJiraRestClient restClient;

	@Mock
	KerberosClient kerberosClient;

	@InjectMocks
	private JiraClientService jiraClientService;

	@Before
	public void setUp() {
	}

	@Test
	public void testRestClientOperations() {
		String projectId = "project123";

		assertFalse(jiraClientService.isContainRestClient(projectId));

		jiraClientService.setRestClientMap(projectId, restClient);

		assertTrue(jiraClientService.isContainRestClient(projectId));
		assertEquals(restClient, jiraClientService.getRestClientMap(projectId));

		jiraClientService.removeRestClientMapClientForKey(projectId);

		assertFalse(jiraClientService.isContainRestClient(projectId));
		assertNull(jiraClientService.getRestClientMap(projectId));
	}

	@Test
	public void testKerberosClientOperations() {
		String projectId = "project456";

		assertFalse(jiraClientService.isContainKerberosClient(projectId));

		jiraClientService.setKerberosClientMap(projectId, kerberosClient);

		assertTrue(jiraClientService.isContainKerberosClient(projectId));
		assertEquals(kerberosClient, jiraClientService.getKerberosClientMap(projectId));

		jiraClientService.removeKerberosClientMapClientForKey(projectId);

		assertFalse(jiraClientService.isContainKerberosClient(projectId));
		assertNull(jiraClientService.getKerberosClientMap(projectId));
	}
}
