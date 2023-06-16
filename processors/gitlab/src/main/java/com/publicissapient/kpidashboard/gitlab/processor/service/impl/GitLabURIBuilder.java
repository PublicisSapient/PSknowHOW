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

package com.publicissapient.kpidashboard.gitlab.processor.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.gitlab.config.GitLabConfig;
import com.publicissapient.kpidashboard.gitlab.constants.GitLabConstants;
import com.publicissapient.kpidashboard.gitlab.model.GitLabRepo;

/**
 * The Class GitLabURIBuilder.
 */
public class GitLabURIBuilder {
	/** The repo. */
	@Autowired
	GitLabRepo repo;
	/** The repo. */
	@Autowired
	ProcessorToolConnection gitLabInfo;

	/** The config. */
	@Autowired
	GitLabConfig config;

	/**
	 * Instantiates a new gitlab URI builder.
	 *
	 * @param repo
	 *            the repo
	 * @param config
	 *            the config
	 */
	public GitLabURIBuilder(GitLabRepo repo, GitLabConfig config, ProcessorToolConnection gitLabInfo) {
		this.repo = repo;
		this.config = config;
		this.gitLabInfo = gitLabInfo;
	}

	/**
	 * Builds the.
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
		final URIBuilder builder = new URIBuilder(scheme + "://" + uri.getHost() + getPath());
		if (uri.getPort() > 0) {
			builder.setPort(uri.getPort());
		}

		getParams().forEach(builder::addParameter);

		return builder.build().toString();
	}

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
		map.put("ref_name",
				StringUtils.isNotEmpty(gitLabInfo.getBranch()) ? gitLabInfo.getBranch().replace(" ", "%20") : "master");
		if (StringUtils.isNotEmpty(repo.getLastCommitTimestamp())) {

			ZonedDateTime zdt1 = ZonedDateTime
					.ofInstant(Instant.ofEpochMilli(Long.valueOf(repo.getLastCommitTimestamp())), ZoneId.of("UTC"));
			String datetime = zdt1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
			map.put("since", datetime);
		}
		map.put("per_page", GitLabConstants.PER_PAGE_SIZE);
		return map;
	}

	private Map<String, String> getExtraParams() {
		Map<String, String> map = new HashMap<>();
		map.put("ref_name",
				StringUtils.isNotEmpty(gitLabInfo.getBranch()) ? gitLabInfo.getBranch().replace(" ", "%20") : "master");

		map.put("per_page", GitLabConstants.PER_PAGE_SIZE);
		return map;
	}

	/**
	 * Gets the path.
	 *
	 * @param uri
	 *            the uri
	 * @return the path
	 */
	private String getPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(GitLabConstants.GITLAB_API).append("/" + repo.getGitLabProjectId())
				.append(GitLabConstants.GITLAB_URL_API_REPO).append(GitLabConstants.GITLAB_URL_API_COMMIT);
		return sb.toString();
	}

	private String getMRPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(GitLabConstants.GITLAB_API).append("/" + repo.getGitLabProjectId())
				.append(GitLabConstants.GITLAB_URL_API_MERGEREQUEST);
		return sb.toString();
	}

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	private URI getURI() {
		String url = gitLabInfo.getUrl();
		url = StringUtils.removeEnd(url, ".git");
		return URI.create(url.replace(" ", "%20"));
	}

}
