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
