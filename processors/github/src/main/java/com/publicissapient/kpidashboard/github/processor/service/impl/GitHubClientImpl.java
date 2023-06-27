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

package com.publicissapient.kpidashboard.github.processor.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.common.constant.CommitType;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.github.config.GitHubConfig;
import com.publicissapient.kpidashboard.github.constants.GitHubConstants;
import com.publicissapient.kpidashboard.github.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.github.model.GitHubProcessorItem;
import com.publicissapient.kpidashboard.github.processor.service.GitHubClient;
import com.publicissapient.kpidashboard.gitlab.util.GitHubURIBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author narsingh9
 *
 */
@Slf4j
@Component
public class GitHubClientImpl implements GitHubClient {

	private static final String PAGE_PARAM = "&page=";
	@Autowired
	private GitHubConfig gitLabConfig;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private AesEncryptionService aesEncryptionService;

	/**
	 * Decrypt apiToken.
	 *
	 * @param apiToken
	 *            the encrypted apiToken
	 * @return the string
	 * @throws GeneralSecurityException
	 *             the GeneralSecurityException
	 */
	protected String decryptApiToken(String apiToken) throws GeneralSecurityException {
		return StringUtils.isNotEmpty(apiToken)
				? aesEncryptionService.decrypt(apiToken, gitLabConfig.getAesEncryptionKey())
				: "";
	}

	/**
	 * Fetch all commits.
	 *
	 * @param gitHubProcessorItem
	 *            the repo
	 * @param firstRun
	 *            the first run
	 * @param githubToolConnection
	 *            tool and connections info
	 * @return the list
	 * 
	 * @throws FetchingCommitException
	 *             the exception
	 */
	public List<CommitDetails> fetchAllCommits(GitHubProcessorItem gitHubProcessorItem, boolean firstRun,
			ProcessorToolConnection githubToolConnection, ProjectBasicConfig proBasicConfig)
			throws FetchingCommitException {

		String restUri = null;
		List<CommitDetails> commits = new ArrayList<>();
		try {
			String decryptedApiToken = decryptApiToken(githubToolConnection.getAccessToken());
			String restUrl = new GitHubURIBuilder(githubToolConnection).build();
			restUri = URLDecoder.decode(restUrl, "UTF-8");
			log.debug("REST URL {}", restUri);
			boolean hasMorePage = true;
			int nextPage = 1;
			while (hasMorePage) {
				ResponseEntity<String> respPayload = getResponse(githubToolConnection.getUsername(), decryptedApiToken,
						restUri);
				if (respPayload == null)
					break;
				JSONArray responseJson = getJSONFromResponse(respPayload.getBody());
				initializeCommitDetails(githubToolConnection, commits, responseJson, proBasicConfig);
				nextPage++;
				if (StringUtils.containsIgnoreCase(restUri, PAGE_PARAM)) {
					restUri = restUri.replace(PAGE_PARAM + (nextPage - 1), PAGE_PARAM + nextPage);
				} else {
					restUri = restUri.concat(PAGE_PARAM + nextPage);
				}
				if (responseJson.isEmpty()) {
					hasMorePage = false;
				}
			}
		} catch (URISyntaxException | RestClientException | GeneralSecurityException | ParseException
				| UnsupportedEncodingException ex) {
			log.error("Failed to fetch commit details ", ex);
			throw new FetchingCommitException("Failed to fetch commits", ex);
		}
		gitHubProcessorItem.setUpdatedTime(System.currentTimeMillis());
		log.info("Commits Recieved From Server for project {}->{}", proBasicConfig.getProjectName(), commits.size());
		return commits;
	}

	private void initializeCommitDetails(ProcessorToolConnection gitLabInfo, List<CommitDetails> commits,
			JSONArray jsonArray, ProjectBasicConfig proBasicConfig) {
		for (Object jsonObj : jsonArray) {
			JSONObject commitObjectt = (JSONObject) jsonObj;
			String scmRevisionNumber = getString(commitObjectt, GitHubConstants.RESP_ID_KEY);
			JSONObject commitObject = (JSONObject) commitObjectt.get(GitHubConstants.RESP_COMMIT);
			String message = getString(commitObject, GitHubConstants.RESP_MESSAGE_KEY);
			JSONObject authorObject = (JSONObject) commitObject.get(GitHubConstants.RESP_AUTHOR_KEY);
			String author = getString(authorObject, GitHubConstants.RESP_NAME_KEY);
			String strDateTime = getString(authorObject, GitHubConstants.RESP_AUTHOR_TIMESTAMP_KEY);
			LocalDateTime parsedDate = LocalDateTime.parse(strDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
			ZonedDateTime zdt = ZonedDateTime.of(parsedDate, ZoneId.of("UTC"));
			long timestamp = zdt.toInstant().toEpochMilli();
			JSONArray parents = (JSONArray) commitObjectt.get(GitHubConstants.RESP_PARENTS_KEY);
			List<String> parentList = new ArrayList<>();
			if (null != parents && !parents.isEmpty()) {
				for (int index = 0; index < parents.size(); index++) {
					JSONObject parentObject = (JSONObject) parents.get(index);
					parentList.add(getString(parentObject, GitHubConstants.RESP_ID_KEY));
				}
			}
			commitDetails(gitLabInfo, commits, scmRevisionNumber, message, author, timestamp, parentList,
					proBasicConfig);

		}
	}

	@Override
	public List<MergeRequests> fetchMergeRequests(GitHubProcessorItem gitHubProcessorItem, boolean firstRun,
			ProcessorToolConnection processorToolConnection, ProjectBasicConfig proBasicConfig)
			throws FetchingCommitException {

		String restUri = null;
		List<MergeRequests> mergeRequests = new ArrayList<>();
		try {
			String decryptedApiToken = decryptApiToken(processorToolConnection.getAccessToken());
			String restUrl = new GitHubURIBuilder(processorToolConnection).mergeRequestUrlbuild();
			restUri = URLDecoder.decode(restUrl, "UTF-8");
			log.debug("REST URL {}", restUri);
			boolean hasMorePage = true;
			int nextPage = 1;
			while (hasMorePage) {
				ResponseEntity<String> respPayload = getResponse(processorToolConnection.getUsername(),
						decryptedApiToken, restUri);
				if (respPayload == null)
					break;
				JSONArray responseJson = getJSONFromResponse(respPayload.getBody());
				initializeMergeRequestDetails(processorToolConnection, mergeRequests, responseJson, proBasicConfig);
				nextPage++;
				if (StringUtils.containsIgnoreCase(restUri, PAGE_PARAM)) {
					restUri = restUri.replace(PAGE_PARAM + (nextPage - 1), PAGE_PARAM + nextPage);
				} else {
					restUri = restUri.concat(PAGE_PARAM + nextPage);
				}

				if (responseJson.isEmpty()) {
					hasMorePage = false;
				}
			}
		} catch (URISyntaxException | RestClientException | GeneralSecurityException | ParseException
				| UnsupportedEncodingException ex) {
			log.error("Failed to fetch merge request details ", ex);
			throw new FetchingCommitException("Failed to fetch merge request", ex);
		}
		gitHubProcessorItem.setUpdatedTime(System.currentTimeMillis());
		log.info("Merge Requests From Server for project {}->{}", proBasicConfig.getProjectName(),
				mergeRequests.size());
		return mergeRequests;
	}

	private void initializeMergeRequestDetails(ProcessorToolConnection gitLabInfo, List<MergeRequests> mergeRequestList,
			JSONArray jsonArray, ProjectBasicConfig proBasicConfig) {
		for (Object jsonObj : jsonArray) {
			long closedDate = 0;
			JSONObject mergReqObj = (JSONObject) jsonObj;
			String title = getString(mergReqObj, GitHubConstants.RESP_TITLE);
			String state = getString(mergReqObj, GitHubConstants.RESP_STATE).toUpperCase();
			boolean isOpen = Boolean.parseBoolean(getString(mergReqObj, GitHubConstants.RESP_OPEN));
			boolean isClosed = Boolean.parseBoolean(getString(mergReqObj, GitHubConstants.RESP_CLOSED));
			long createdDate = getDateTimeStamp(getString(mergReqObj, GitHubConstants.RESP_CREATED_AT));
			long updatedDate = getDateTimeStamp(getString(mergReqObj, GitHubConstants.RESP_UPDATED_AT));
			if (getString(mergReqObj, GitHubConstants.RESP_CLOSED_AT) != null) {
				closedDate = getDateTimeStamp(getString(mergReqObj, GitHubConstants.RESP_CLOSED_AT));
			}
			if (getString(mergReqObj, GitHubConstants.RESP_MERGED_AT) != null) {
				closedDate = getDateTimeStamp(getString(mergReqObj, GitHubConstants.RESP_MERGED_AT));
				state = GitHubConstants.MERGED;
			}

			JSONObject fromBranchObj = (JSONObject) mergReqObj.get(GitHubConstants.RESP_HEAD);
			JSONObject toBranchObj = (JSONObject) mergReqObj.get(GitHubConstants.RESP_BASE);
			String fromBranch = getString(fromBranchObj, GitHubConstants.RESP_REF);
			String toBranch = getString(toBranchObj, GitHubConstants.RESP_REF);

			String repoSlug = "NA";
			String projKey = gitLabInfo.getRepositoryName();
			JSONObject authorObj = (JSONObject) mergReqObj.get(GitHubConstants.RESP_USER);
			String author = getString(authorObj, GitHubConstants.RESP_LOGIN);
			String scmRevisionNumber = getString(mergReqObj, GitHubConstants.RESP_NUMBER);
			JSONArray reviewers = (JSONArray) mergReqObj.get(GitHubConstants.RESP_REQUESTED_REVIEWERS);
			List<String> reviewersList = new ArrayList<>();
			if (reviewers != null) {
				for (Object reviewersObj : reviewers) {
					reviewersList.add(getString((JSONObject) reviewersObj, GitHubConstants.RESP_ID));
				}
			}
			MergeRequests mergeReq = new MergeRequests();
			mergeReq.setTitle(title);
			mergeReq.setState(state);
			mergeReq.setOpen(isOpen);
			mergeReq.setClosed(isClosed);
			mergeReq.setCreatedDate(createdDate);
			mergeReq.setUpdatedDate(updatedDate);
			mergeReq.setClosedDate(closedDate);
			mergeReq.setFromBranch(fromBranch);
			mergeReq.setToBranch(toBranch);
			mergeReq.setRepoSlug(repoSlug);
			mergeReq.setProjKey(projKey);
			if (proBasicConfig.isSaveAssigneeDetails()) {
				mergeReq.setAuthor(author);
			}
			mergeReq.setRevisionNumber(scmRevisionNumber);
			mergeReq.setReviewers(reviewersList);
			mergeRequestList.add(mergeReq);
		}
	}

	Long getDateTimeStamp(String datestring) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date;
		long timestamp = 0;
		try {
			date = formatter.parse(datestring);
			timestamp = date.getTime();
		} catch (java.text.ParseException e) {

			log.error("error while parsing date", e);
		}
		return timestamp;
	}

	@SuppressWarnings("java:S107")
	private void commitDetails(ProcessorToolConnection gitLabInfo, List<CommitDetails> commits,
			String scmRevisionNumber, String message, String author, long timestamp, List<String> parentList,
			ProjectBasicConfig proBasicConfig) {
		CommitDetails gitLabCommit = new CommitDetails();
		gitLabCommit.setBranch(gitLabInfo.getBranch());
		gitLabCommit.setUrl(gitLabInfo.getUrl());
		gitLabCommit.setTimestamp(System.currentTimeMillis());
		gitLabCommit.setRevisionNumber(scmRevisionNumber);
		if (proBasicConfig.isSaveAssigneeDetails()) {
			gitLabCommit.setAuthor(author);
		}
		gitLabCommit.setCommitLog(message);
		gitLabCommit.setParentRevisionNumbers(parentList);
		gitLabCommit.setCommitTimestamp(timestamp);
		gitLabCommit.setType(parentList.size() > 1 ? CommitType.MERGE : CommitType.NEW);
		commits.add(gitLabCommit);
	}

	/**
	 * Gets the response.
	 *
	 * @param userName
	 *            the user name
	 * @param apiToken
	 *            the GitlabAccessToken
	 * @param url
	 *            the url
	 * @return the response
	 */
	protected ResponseEntity<String> getResponse(String userName, String apiToken, String url) {
		HttpEntity<HttpHeaders> httpEntity = null;
		if (userName != null && apiToken != null) {

			final HttpHeaders privateToken = new HttpHeaders();
			privateToken.set("Authorization", "token " + apiToken);
			httpEntity = new HttpEntity<>(privateToken);
		}
		return restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
	}

	/**
	 * Gets the string.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param key
	 *            the key
	 * @return the string
	 */
	protected String getString(JSONObject jsonObject, String key) {
		String val = null;
		final Object objectVal = jsonObject.get(key);
		if (objectVal != null) {
			val = objectVal.toString();
		}
		return val;
	}

	/**
	 * Gets the JSON from response.
	 *
	 * @param payload
	 *            the payload
	 * @return the JSON from response
	 * @throws ParseException
	 *             the ParseException
	 */
	protected JSONArray getJSONFromResponse(String payload) throws ParseException {
		JSONParser parser = new JSONParser();
		return (JSONArray) parser.parse(payload);
	}

}
