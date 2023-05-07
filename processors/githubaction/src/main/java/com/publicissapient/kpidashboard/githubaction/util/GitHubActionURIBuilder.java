package com.publicissapient.kpidashboard.githubaction.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

public class GitHubActionURIBuilder {

	private final ProcessorToolConnection gitHubActionToolConnection;

	public GitHubActionURIBuilder(ProcessorToolConnection toolConnection) {
		this.gitHubActionToolConnection = toolConnection;
	}

	public String deployGithub() throws URISyntaxException {
		URI uri = getURI();
		String scheme = "ssh".equalsIgnoreCase(uri.getScheme()) ? "https" : uri.getScheme();
		final URIBuilder builder = new URIBuilder(scheme + "://" + uri.getHost() + getDeployPath());
		if (uri.getPort() > 0) {
			builder.setPort(uri.getPort());
		}

		return builder.build().toString();
	}

	public String build() throws URISyntaxException {
		URI uri = getURI();
		String scheme = "ssh".equalsIgnoreCase(uri.getScheme()) ? "https" : uri.getScheme();
		final URIBuilder builder = new URIBuilder(scheme + "://" + uri.getHost() + getBuildPath());
		if (uri.getPort() > 0) {
			builder.setPort(uri.getPort());
		}

		return builder.build().toString();
	}

	private URI getURI() {
		String url = gitHubActionToolConnection.getUrl();
		url = StringUtils.removeEnd(url, ".git");
		return URI.create(url.replace(" ", "%20"));
	}

	private String getBuildPath() {
		StringBuilder sb = new StringBuilder();
		sb.append("/repos/" + gitHubActionToolConnection.getUsername() + "/"
				+ gitHubActionToolConnection.getRepositoryName() + "/actions/workflows/"
				+ gitHubActionToolConnection.getWorkflowID() + "/runs");
		return sb.toString();
	}

	private String getDeployPath() {
		StringBuilder sb = new StringBuilder();
		sb.append("/repos/" + gitHubActionToolConnection.getUsername() + "/"
				+ gitHubActionToolConnection.getRepositoryName() + "/deployments");
		return sb.toString();
	}

}
