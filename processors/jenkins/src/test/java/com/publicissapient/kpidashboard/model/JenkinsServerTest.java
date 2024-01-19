package com.publicissapient.kpidashboard.model;

import com.publicissapient.kpidashboard.jenkins.model.JenkinsServer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JenkinsServerTest {

	@Test
	public void testNoArgsConstructor() {
		JenkinsServer jenkinsServer = new JenkinsServer();
		assertNotNull(jenkinsServer);
	}

	@Test
	public void testAllArgsConstructor() {
		JenkinsServer jenkinsServer = new JenkinsServer("http://example.com", "127.0.0.1", "user", "api_key");
		assertNotNull(jenkinsServer);
		assertEquals("http://example.com", jenkinsServer.getUrl());
		assertEquals("127.0.0.1", jenkinsServer.getIpAddress());
		assertEquals("user", jenkinsServer.getUsername());
		assertEquals("api_key", jenkinsServer.getApiKey());
	}

	@Test
	public void testGettersAndSetters() {
		JenkinsServer jenkinsServer = new JenkinsServer();
		jenkinsServer.setUrl("http://example.com");
		jenkinsServer.setIpAddress("127.0.0.1");
		jenkinsServer.setUsername("user");
		jenkinsServer.setApiKey("api_key");

		assertEquals("http://example.com", jenkinsServer.getUrl());
		assertEquals("127.0.0.1", jenkinsServer.getIpAddress());
		assertEquals("user", jenkinsServer.getUsername());
		assertEquals("api_key", jenkinsServer.getApiKey());
	}

}
