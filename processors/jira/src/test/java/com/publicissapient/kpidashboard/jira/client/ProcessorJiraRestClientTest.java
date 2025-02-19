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

package com.publicissapient.kpidashboard.jira.client;

import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.*;

@RunWith(MockitoJUnitRunner.class)
public class ProcessorJiraRestClientTest {

	static ProcessorJiraRestClient processorJiraRestClient;

	@BeforeClass
	public static void processorJiraRestClientSetup() {
		processorJiraRestClient = new ProcessorJiraRestClient() {
			@Override
			public IssueRestClient getIssueClient() {
				return null;
			}

			@Override
			public SessionRestClient getSessionClient() {
				return null;
			}

			@Override
			public UserRestClient getUserClient() {
				return null;
			}

			@Override
			public GroupRestClient getGroupClient() {
				return null;
			}

			@Override
			public ProjectRestClient getProjectClient() {
				return null;
			}

			@Override
			public ComponentRestClient getComponentClient() {
				return null;
			}

			@Override
			public MetadataRestClient getMetadataClient() {
				return null;
			}

			@Override
			public SearchRestClient getSearchClient() {
				return null;
			}

			@Override
			public VersionRestClient getVersionRestClient() {
				return null;
			}

			@Override
			public ProjectRolesRestClient getProjectRolesRestClient() {
				return null;
			}

			@Override
			public AuditRestClient getAuditRestClient() {
				return null;
			}

			@Override
			public MyPermissionsRestClient getMyPermissionsRestClient() {
				return null;
			}

			@Override
			public void close() throws IOException {
			}

			@Override
			public SearchRestClient getProcessorSearchClient() {
				return null;
			}

			@Override
			public CustomAsynchronousIssueRestClient getCustomIssueClient() {
				return null;
			}
		};
	}

	@Test
	public void getProcessorSearchClienTest() {
		assertNull(processorJiraRestClient.getProcessorSearchClient());
	}

	@Test
	public void getCustomIssueClientTest() {
		assertNull(processorJiraRestClient.getCustomIssueClient());
	}

	@AfterClass
	public static void processorJiraRestClientClean() {
		processorJiraRestClient = null;
	}
}
