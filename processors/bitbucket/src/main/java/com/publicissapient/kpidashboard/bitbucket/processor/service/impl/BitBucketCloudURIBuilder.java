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

package com.publicissapient.kpidashboard.bitbucket.processor.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import com.publicissapient.kpidashboard.bitbucket.config.BitBucketConfig;
import com.publicissapient.kpidashboard.bitbucket.model.BitbucketRepo;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

/**
 * The Class BitBucketServerURIBuilder.
 */
public class BitBucketCloudURIBuilder {

	private static final int PATH_LENGTH_LIMIT_FOR_SCM_REMOVAL = 5;
	/** The repo. */
	private final BitbucketRepo repo;

	/** The config. */
	private final BitBucketConfig config;

	/** Bitbucket Server Detail for url,pass etc */
	private final ProcessorToolConnection bitBucketServerInfo;

	/**
	 * Instantiates a new bit bucket server URI builder.
	 *
	 * @param repo
	 *            the repo
	 * @param config
	 *            the config
	 * @param bitBucketServerInfo
	 *            the bitBucketServerInfo
	 */
	public BitBucketCloudURIBuilder(BitbucketRepo repo, BitBucketConfig config,
			ProcessorToolConnection bitBucketServerInfo) {
		this.repo = repo;
		this.config = config;
		this.bitBucketServerInfo = bitBucketServerInfo;
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
		final URIBuilder builder = new URIBuilder();
		URI uri = getURIfromBitbucketInfo();

		String scheme = "ssh".equalsIgnoreCase(uri.getScheme()) ? "https" : uri.getScheme();
		builder.setScheme(scheme).setHost(uri.getHost()).setPath(getPath());
		if (uri.getPort() > 0) {
			builder.setPort(uri.getPort());
		}

		getParams().forEach(builder::addParameter);

		return builder.build().toString();
	}

	/**
	 * @return URL
	 * @throws URISyntaxException
	 *             the URISyntaxException
	 */
	public String buildMergeReqURL() throws URISyntaxException {
		final URIBuilder builder = new URIBuilder();
		URI uri = getURIfromBitbucketInfo();

		String scheme = "ssh".equalsIgnoreCase(uri.getScheme()) ? "https" : uri.getScheme();
		builder.setScheme(scheme).setHost(uri.getHost()).setPath(getMergeRequestsPath());
		if (uri.getPort() > 0) {
			builder.setPort(uri.getPort());
		}
		getParamsMerge().forEach(builder::addParameter);
		return builder.build().toString();
	}

	/**
	 * Gets the params.
	 *
	 * @return the params
	 */
	private Map<String, String> getParams() {
		Map<String, String> map = new HashMap<>();
		map.put("include",
				StringUtils.isNotEmpty(bitBucketServerInfo.getBranch())
						? bitBucketServerInfo.getBranch().replace(" ", "%20")
						: "master");

		if (config.getPageSize() > 0) {
			map.put("paglen", String.valueOf(config.getPageSize()));
		}

		return map;
	}

	/**
	 * Gets the merge params.
	 *
	 * @return the merge params
	 */
	private Map<String, String> getParamsMerge() {
		Map<String, String> map = new HashMap<>();
		if (config.getPageSizeCloudPull() > 0) {
			map.put("paglen", String.valueOf(config.getPageSize()));
		}
		if (StringUtils.isNotBlank(config.getStatusCloudPull())) {
			map.put("state", String.valueOf(config.getStatusCloudPull()));
		}

		return map;
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	private String getPath() {
		String apiPath = "";
		if (config.getApi() != null) {
			apiPath = StringUtils.removeEnd(repo.getToolDetailsMap().get("bitbucketApi").toString(), "/");
		}

		StringBuilder sb = new StringBuilder(apiPath);
		sb.append("/repositories/").append(bitBucketServerInfo.getBitbucketProjKey()).append("/")
				.append(bitBucketServerInfo.getRepoSlug()).append("/commits/");
		return sb.toString();
	}

	/**
	 * @return path
	 */
	private String getMergeRequestsPath() {
		String apiPath = "";
		if (config.getApi() != null) {
			apiPath = StringUtils.removeEnd(repo.getToolDetailsMap().get("bitbucketApi").toString(), "/");
		}

		StringBuilder sb = new StringBuilder(apiPath);
		sb.append("/repositories/").append(bitBucketServerInfo.getBitbucketProjKey()).append("/")
				.append(bitBucketServerInfo.getRepoSlug()).append("/pullrequests/");
		return sb.toString();
	}

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	@SuppressWarnings("unused")
	private URI getURI() {
		String url = repo.getRepoUrl();
		url = StringUtils.removeEnd(url, ".git");
		return URI.create(url.replace(" ", "%20"));
	}

	/**
	 * Gets the uri from Connection
	 *
	 * @return the uri
	 */
	private URI getURIfromBitbucketInfo() {
		String url = bitBucketServerInfo.getUrl();
		url = StringUtils.removeEnd(url, ".git");
		return URI.create(url.replace(" ", "%20"));
	}
}
