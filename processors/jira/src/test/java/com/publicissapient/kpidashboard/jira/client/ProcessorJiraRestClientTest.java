package com.publicissapient.kpidashboard.jira.client;

import com.atlassian.jira.rest.client.api.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
