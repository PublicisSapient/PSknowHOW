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

package com.publicissapient.kpidashboard.gitlab.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.github.constants.GitHubConstants;

/**
 * The Class GitLabURIBuilder.
 */
public class GitHubURIBuilder {

	/** The repo. */
	private final ProcessorToolConnection gitHubToolConnection;

	/**
	 * Instantiates a new gitlab URI builder.
	 *
	 * @param gitHubToolConnection
	 *            the config
	 */
	public GitHubURIBuilder(ProcessorToolConnection gitHubToolConnection) {
		this.gitHubToolConnection = gitHubToolConnection;
	}

	/**
	 * Builds commit path.
	 *
	 * @return the string
	 * @throws URISyntaxException
	 *             uri syntax exception
	 * @throws URISyntaxException
	 *             the URISyntaxException
	 */
	public String build() throws URISyntaxException {
		URI uri = getURI();
		String scheme = "ssh".equalsIgnoreCase(uri.getScheme()) ? "https" : uri.getScheme();
		final URIBuilder builder = new URIBuilder(scheme + "://" + uri.getHost() + getCommitPath());
		if (uri.getPort() > 0) {
			builder.setPort(uri.getPort());
		}

		getParams().forEach(builder::addParameter);

		return builder.build().toString();
	}

	/**
	 * Builds merge request url.
	 *
	 * @return the string
	 * @throws URISyntaxException
	 *             uri syntax exception
	 * @throws URISyntaxException
	 *             the URISyntaxException
	 */
	public String mergeRequestUrlbuild() throws URISyntaxException {
		URI uri = getURI();
		String scheme = "ssh".equalsIgnoreCase(uri.getScheme()) ? "https" : uri.getScheme();
		final URIBuilder builder = new URIBuilder(scheme + "://" + uri.getHost() + getMRPath());
		if (uri.getPort() > 0) {
			builder.setPort(uri.getPort());
		}

		getExtraParams().forEach(builder::addParameter);

		return builder.build().toString();
	}

	/**
	 * Gets the params.
	 *
	 * @return the params
	 */
	private Map<String, String> getParams() {
		Map<String, String> map = new HashMap<>();
		map.put("sha",
				StringUtils.isNotEmpty(gitHubToolConnection.getBranch())
						? gitHubToolConnection.getBranch().replace(" ", "%20")
						: "master");
		map.put("per_page", GitHubConstants.PER_PAGE_SIZE);
		return map;
	}

	private Map<String, String> getExtraParams() {
		Map<String, String> map = new HashMap<>();
		map.put("state", "all");
		map.put("base",
				StringUtils.isNotEmpty(gitHubToolConnection.getBranch())
						? gitHubToolConnection.getBranch().replace(" ", "%20")
						: "master");

		map.put("per_page", GitHubConstants.PER_PAGE_SIZE);
		return map;
	}

	/**
	 * Gets the path.
	 *
	 * @param uri
	 *            the uri
	 * @return the path
	 */
	private String getCommitPath() {
		StringBuilder sb = new StringBuilder();
		sb.append("/repos/" + gitHubToolConnection.getUsername() + "/" + gitHubToolConnection.getRepositoryName()
				+ "/commits");
		return sb.toString();
	}

	private String getMRPath() {
		StringBuilder sb = new StringBuilder();
		sb.append("/repos/" + gitHubToolConnection.getUsername() + "/" + gitHubToolConnection.getRepositoryName()
				+ "/pulls");
		return sb.toString();
	}

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	private URI getURI() {
		String url = gitHubToolConnection.getUrl();
		url = StringUtils.removeEnd(url, ".git");
		return URI.create(url.replace(" ", "%20"));
	}

}
