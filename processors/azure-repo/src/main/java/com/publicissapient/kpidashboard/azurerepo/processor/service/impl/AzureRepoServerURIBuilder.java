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

package com.publicissapient.kpidashboard.azurerepo.processor.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import com.publicissapient.kpidashboard.azurerepo.config.AzureRepoConfig;
import com.publicissapient.kpidashboard.azurerepo.model.AzureRepoModel;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

/**
 * The Class AzureRepoServerURIBuilder.
 */
public class AzureRepoServerURIBuilder {

	private static final String FORWARDSLASH = "/";

	/** The repo. */
	private final AzureRepoModel repo;

	/** The config . */
	private final AzureRepoConfig config;
	/** The ProcessorToolConnection . */
	private final ProcessorToolConnection azureRepoProcessor;

	/**
	 * Instantiates a new azure repo server URI builder.
	 * 
	 * @param repo
	 *            AzureRepoConfig
	 * @param config
	 *            config
	 * @param azureRepoProcessor
	 *            azureRepoProcessor
	 */
	public AzureRepoServerURIBuilder(AzureRepoModel repo, AzureRepoConfig config,
			ProcessorToolConnection azureRepoProcessor) {
		this.repo = repo;
		this.config = config;
		this.azureRepoProcessor = azureRepoProcessor;
	}

	/**
	 * Builds the.
	 *
	 * @return the string
	 * @throws URISyntaxException
	 *             the URISyntaxException
	 */
	public String build() throws URISyntaxException {
		final URIBuilder builder = new URIBuilder();
		URI uri = getURIfromAzureRepo();

		String scheme = "ssh".equalsIgnoreCase(uri.getScheme()) ? "https" : uri.getScheme();

		String path = getPath(uri);
		String host = uri.getHost();
		builder.setScheme(scheme).setHost(host).setPath(path);
		if (uri.getPort() > 0) {
			builder.setPort(uri.getPort());
		}

		getParams().forEach(builder::addParameter);

		String urlNew = builder.build().toString(); // builds the uri after all the
		if (config.getPageSize() > 0) {
			urlNew = urlNew.concat("&$top=").concat(String.valueOf(config.getPageSize()));
		}

		return urlNew;
	}

	public String mergeRequestUrlbuild() throws URISyntaxException {
		final URIBuilder builder = new URIBuilder();
		URI uri = getURIfromAzureRepo();

		String scheme = "ssh".equalsIgnoreCase(uri.getScheme()) ? "https" : uri.getScheme();

		String path = getMRPath(uri);
		String host = uri.getHost();
		builder.setScheme(scheme).setHost(host).setPath(path);
		if (uri.getPort() > 0) {
			builder.setPort(uri.getPort());
		}

		getMergeRequestParams().forEach(builder::addParameter);

		String urlNew = builder.build().toString(); // builds the uri after all the
		if (config.getPageSize() > 0) {
			urlNew = urlNew.concat("&$top=").concat(String.valueOf(config.getPageSize()));
		}

		return urlNew;
	}

	private Map<String, String> getMergeRequestParams() { // additional parameter add to
		// builder type
		Map<String, String> map = new HashMap<>();

		map.put("api.Version", String.valueOf(azureRepoProcessor.getApiVersion()));

		if (StringUtils.isNotEmpty(repo.getLastUpdatedCommit())) {
			map.put("searchCriteria.compareVersion.version",
					StringUtils.isNotEmpty(azureRepoProcessor.getBranch())
							? azureRepoProcessor.getBranch().replace(" ", "%20")
							: "master");
			map.put("searchCriteria.compareVersion.versionType", "branch");

			map.put("searchCriteria.itemVersion.version", repo.getLastUpdatedCommit().replace(" ", "%20"));
			map.put("searchCriteria.itemVersion.versionType", "pullrequests");
		} else {
			map.put("searchCriteria.itemVersion.version",
					StringUtils.isNotEmpty(azureRepoProcessor.getBranch())
							? azureRepoProcessor.getBranch().replace(" ", "%20")
							: "master");
		}
		return map;
	}

	/**
	 * Gets the params.
	 *
	 * @return the params
	 */
	private Map<String, String> getParams() { // additional parameter add to
											  // builder type
		Map<String, String> map = new HashMap<>();

		map.put("api.Version", String.valueOf(azureRepoProcessor.getApiVersion()));

		if (StringUtils.isNotEmpty(repo.getLastUpdatedCommit())) {
			map.put("searchCriteria.compareVersion.version",
					StringUtils.isNotEmpty(azureRepoProcessor.getBranch())
							? azureRepoProcessor.getBranch().replace(" ", "%20")
							: "master");
			map.put("searchCriteria.compareVersion.versionType", "branch");

			map.put("searchCriteria.itemVersion.version", repo.getLastUpdatedCommit().replace(" ", "%20"));
			map.put("searchCriteria.itemVersion.versionType", "commit");
		} else {
			map.put("searchCriteria.itemVersion.version",
					StringUtils.isNotEmpty(azureRepoProcessor.getBranch())
							? azureRepoProcessor.getBranch().replace(" ", "%20")
							: "master");
		}
		return map;
	}

	/**
	 * Gets the path.
	 *
	 * @param uri
	 *            the uri
	 * @return the path
	 */
	private String getPath(URI uri) {

		String repoPath = uri.getPath();
		if (repoPath.lastIndexOf('/') != repoPath.length() - 1) {

			repoPath = repoPath + FORWARDSLASH;
		}
		String apiPath = "";
		if (config.getApi() != null) {

			apiPath = String.valueOf(config.getApi().toString()); // NOSONAR
		}

		StringBuilder sb = new StringBuilder(apiPath);

		sb.append(FORWARDSLASH).append(azureRepoProcessor.getRepositoryName()).append("/commits");
		StringBuilder repoPathFinal = new StringBuilder(repoPath);
		return repoPathFinal.append(sb).toString();
	}

	private String getMRPath(URI uri) {

		String repoPath = uri.getPath();
		if (repoPath.lastIndexOf('/') != repoPath.length() - 1) {

			repoPath = repoPath + FORWARDSLASH;
		}
		String apiPath = "";
		if (config.getApi() != null) {

			apiPath = String.valueOf(config.getApi().toString()); // NOSONAR
		}

		StringBuilder sb = new StringBuilder(apiPath);

		sb.append(FORWARDSLASH).append(azureRepoProcessor.getRepositoryName()).append("/pullrequests");
		StringBuilder repoPathFinal = new StringBuilder(repoPath);
		return repoPathFinal.append(sb).toString();
	}

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	private URI getURIfromAzureRepo() {
		String url = azureRepoProcessor.getUrl();
		return URI.create(url.replace(" ", "%20"));
	}
}