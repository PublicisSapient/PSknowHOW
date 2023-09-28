package com.publicissapient.kpidashboard.jira.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JiraInfoTest {
	@Test
	public void testGettersAndSetters() {
		JiraInfo jiraInfo = JiraInfo.builder().username("testUser").password("testPassword")
				.jiraConfigBaseUrl("https://example.com").jiraConfigProxyUrl("https://proxy.com")
				.jiraConfigProxyPort("8080").jiraConfigAccessToken("token").bearerToken(true).build();
		assertEquals("testUser", jiraInfo.getUsername());
		assertEquals("testPassword", jiraInfo.getPassword());
		assertEquals("https://example.com", jiraInfo.getJiraConfigBaseUrl());
		assertEquals("https://proxy.com", jiraInfo.getJiraConfigProxyUrl());
		assertEquals("8080", jiraInfo.getJiraConfigProxyPort());
		assertEquals("token", jiraInfo.getJiraConfigAccessToken());
	}

	@Test
	public void testBuilder() {
		JiraInfo jiraInfo = JiraInfo.builder().username("testUser").password("testPassword").build();

		assertEquals("testUser", jiraInfo.getUsername());
		assertEquals("testPassword", jiraInfo.getPassword());
	}
}
