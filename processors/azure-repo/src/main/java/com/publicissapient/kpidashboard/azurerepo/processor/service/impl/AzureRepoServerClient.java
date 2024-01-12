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

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.publicissapient.kpidashboard.azurerepo.config.AzureRepoConfig;
import com.publicissapient.kpidashboard.azurerepo.constants.AzureRepoConstants;
import com.publicissapient.kpidashboard.azurerepo.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.azurerepo.model.AzureRepoModel;
import com.publicissapient.kpidashboard.azurerepo.processor.service.AzureRepoClient;
import com.publicissapient.kpidashboard.azurerepo.processor.service.impl.common.BasicAzureRepoClient;
import com.publicissapient.kpidashboard.azurerepo.util.AzureRepoRestOperations;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component

public class AzureRepoServerClient extends BasicAzureRepoClient implements AzureRepoClient {

	private static String skip = "&$skip=";

	/**
	 * Instantiates a new azure repo server client.
	 *
	 * @param config
	 *            the config
	 * @param azurerepoRestOperations
	 *            the rest operations supplier
	 * @param aesEncryptionService
	 *            aesencryptionservice
	 */
	@Autowired
	public AzureRepoServerClient(AzureRepoConfig config, AzureRepoRestOperations azurerepoRestOperations,
			AesEncryptionService aesEncryptionService) {
		super(config, azurerepoRestOperations, aesEncryptionService);
	}

	/**
	 * Fetch all commits.
	 *
	 * @param repo
	 *            the repo
	 * @param firstRun
	 *            the first run
	 * @param azureRepoProcessorInfo
	 *            azure Processor Info Like branch,pat
	 * @return the list
	 * @throws FetchingCommitException
	 *             the exception
	 */
	@Override

	public List<CommitDetails> fetchAllCommits(AzureRepoModel repo, boolean firstRun,
			ProcessorToolConnection azureRepoProcessorInfo, ProjectBasicConfig projectBasicConfig)
			throws FetchingCommitException {// NOSONAR

		String restUri = null;
		List<CommitDetails> commits = new ArrayList<>();
		try {
			String decryptedPat = decryptPat(azureRepoProcessorInfo.getPat());
			boolean isLast = false;
			restUri = new AzureRepoServerURIBuilder(repo, config, azureRepoProcessorInfo).build();
			log.debug("REST URL {}", restUri);
			while (!isLast) {
				ResponseEntity<String> respPayload = getResponse(decryptedPat, restUri);
				if (null != respPayload) {
					JSONObject responseJson = getJSONFromResponse(respPayload.getBody());
					JSONArray jsonArray = (JSONArray) responseJson.get(AzureRepoConstants.RESP_VALUES_KEY);
					String nextPageIndex = setNextPageIndex(restUri);
					initializeCommitDetails(commits, jsonArray, azureRepoProcessorInfo, projectBasicConfig);
					isLast = parseResponse(jsonArray, firstRun, nextPageIndex, responseJson);
					log.info(String.format("Retrieving page : {%s}", nextPageIndex));
					if (Integer.parseInt(nextPageIndex) > 0) {
						restUri = getNextUrl(restUri, nextPageIndex);
					}
				} else {
					isLast = Boolean.TRUE;
				}
			}
			repo.setUpdatedTime(System.currentTimeMillis());

		} catch (URISyntaxException | RestClientException | ParseException ex) {
			log.error("Failed to fetch commit details ", ex);
			throw new FetchingCommitException("Failed to fetch commits", ex);
		}

		return commits;
	}

	private String getNextUrl(String url, String nextPageIndex) {
		String newUrl = null;
		if (url.contains(skip)) {
			newUrl = url.substring(0, url.indexOf(skip));
			newUrl = newUrl.concat(skip).concat(nextPageIndex);
		} else {
			newUrl = url.concat(skip).concat(nextPageIndex);
		}
		return newUrl;
	}

	@Override
	public List<MergeRequests> fetchAllMergeRequest(AzureRepoModel repo, boolean firstRun,
			ProcessorToolConnection azureRepoProcessorInfo, ProjectBasicConfig proBasicConfig)
			throws FetchingCommitException {
		// NOSONAR

		String restUri = null;
		List<MergeRequests> mergeRequests = new ArrayList<>();
		try {
			String decryptedPat = decryptPat(azureRepoProcessorInfo.getPat());
			boolean isLast = false;
			restUri = new AzureRepoServerURIBuilder(repo, config, azureRepoProcessorInfo).mergeRequestUrlbuild();
			log.debug("REST URL {}", restUri);
			while (!isLast) {
				ResponseEntity<String> respPayload = getResponse(decryptedPat, restUri);
				if (null != respPayload) {
					JSONObject responseJson = getJSONFromResponse(respPayload.getBody());
					JSONArray jsonArray = (JSONArray) responseJson.get(AzureRepoConstants.RESP_VALUES_KEY);
					String nextPageIndex = setNextPageIndex(restUri);
					initializeMergeRequestDetails(mergeRequests, jsonArray, proBasicConfig);
					isLast = parseResponse(jsonArray, firstRun, nextPageIndex, responseJson);
					log.info(String.format("Retrieving page : {%s}", nextPageIndex));
					if (Integer.parseInt(nextPageIndex) > 0) {
						restUri = getNextUrl(restUri, nextPageIndex);
					}
				} else {
					isLast = Boolean.TRUE;
				}
			}
			repo.setUpdatedTime(System.currentTimeMillis());

		} catch (URISyntaxException | RestClientException | ParseException ex) {
			log.error("Failed to fetch commit details ", ex);
			throw new FetchingCommitException("Failed to fetch commits", ex);
		}

		return mergeRequests;
	}

	private boolean parseResponse(JSONArray jsonArray, boolean firstRun, String nextPageIndex,
			JSONObject responseJson) {
		boolean isLast = false;
		if (CollectionUtils.isEmpty(jsonArray) || (firstRun
				&& config.getInitialPageSize() <= Integer.parseInt(nextPageIndex == null ? "0" : nextPageIndex))) {
			isLast = true;
		} else {

			String count = getString(responseJson, AzureRepoConstants.COUNT);

			if (Integer.parseInt(count) < config.getPageSize()) {
				isLast = Boolean.TRUE;
			}
		}
		return isLast;
	}

	private String setNextPageIndex(String restUri) {
		String nextPageIndex = StringUtils.EMPTY;
		if (restUri.contains("$skip") || restUri.contains("$top")) {
			nextPageIndex = String.valueOf(config.getPageSize() + (restUri.contains("$skip") ? nextPAge(restUri) : 0));
		}
		return nextPageIndex;
	}

	/* Gives the value of the $skip token in the URL in int form */
	private int nextPAge(String restUri) {

		String restUrl = restUri;
		int start = restUrl.lastIndexOf('=');
		String substringFinal = restUrl.substring(start + 1);
		return (Integer.parseInt(substringFinal));
	}

	private void initializeCommitDetails(List<CommitDetails> commits, JSONArray jsonArray,
			ProcessorToolConnection azureRepoProcessorInfo, ProjectBasicConfig projectBasicConfig) {
		for (Object jsonObj : jsonArray) {

			JSONObject commitObject = (JSONObject) jsonObj;

			String commitId = getString(commitObject, AzureRepoConstants.COMMIT_ID);

			JSONObject authorObject = (JSONObject) commitObject.get(AzureRepoConstants.RESP_AUTHOR_KEY);
			String author = getString(authorObject, AzureRepoConstants.RESP_NAME_KEY);

			String comment = getString(commitObject, AzureRepoConstants.RESP_MESSAGE_KEY);

			JSONObject committerobject = (JSONObject) commitObject.get(AzureRepoConstants.RESP_AUTHOR_COMMITTER);

			String datestring = getString(committerobject, AzureRepoConstants.RESP_AUTHOR_DATE);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date date;
			long timestamp = 0;
			try {

				date = formatter.parse(datestring);
				timestamp = date.getTime();
			} catch (java.text.ParseException e) {

				log.error("error while parsing date", e);
			}

			commitDetails(commits, commitId, comment, author, timestamp, azureRepoProcessorInfo, projectBasicConfig);

		}
	}

	private void initializeMergeRequestDetails(List<MergeRequests> mergeRequestList, JSONArray jsonArray,
			ProjectBasicConfig proBasicConfig) {
		long closedDate = 0;
		long updatedDate = 0;
		long createdDate = 0;
		for (Object jsonObj : jsonArray) {
			JSONObject mergReqObj = (JSONObject) jsonObj;
			String title = getString(mergReqObj, AzureRepoConstants.RESP_TITLE);
			String state = getString(mergReqObj, AzureRepoConstants.RESP_STATUS);
			boolean isOpen = Boolean.parseBoolean(getString(mergReqObj, AzureRepoConstants.RESP_OPEN));
			boolean isClosed = Boolean.parseBoolean(getString(mergReqObj, AzureRepoConstants.RESP_CLOSED));
			if (getString(mergReqObj, AzureRepoConstants.RESP_CREATION_DATE) != null) {
				createdDate = getDateTimeStamp(getString(mergReqObj, AzureRepoConstants.RESP_CREATION_DATE));
			}
			if (getString(mergReqObj, AzureRepoConstants.RESP_UPDATED_DATE) != null) {
				updatedDate = getDateTimeStamp(getString(mergReqObj, AzureRepoConstants.RESP_UPDATED_DATE));
			}
			if (getString(mergReqObj, AzureRepoConstants.RESP_CLOSED_DATE) != null) {
				closedDate = getDateTimeStamp(getString(mergReqObj, AzureRepoConstants.RESP_CLOSED_DATE));
			}

			String fromBranch = getString(mergReqObj, AzureRepoConstants.RESP_SOURCE_REF_NAME);
			String toBranch = getString(mergReqObj, AzureRepoConstants.RESP_TARGET_REF_NAME);
			JSONObject repoObj = (JSONObject) mergReqObj.get(AzureRepoConstants.RESP_REPO);
			String repoSlug = getString(repoObj, AzureRepoConstants.RESP_NAME);
			JSONObject projObj = (JSONObject) repoObj.get(AzureRepoConstants.RESP_PROJ);
			String projKey = getString(projObj, AzureRepoConstants.RESP_NAME_KEY);
			JSONObject authorObj = (JSONObject) mergReqObj.get(AzureRepoConstants.RESP_CREATED_BY);
			String author = getString(authorObj, AzureRepoConstants.RESP_DISP_NAME);
			String scmRevisionNumber = getString(repoObj, AzureRepoConstants.RESP_ID_KEY);
			JSONArray reviewers = (JSONArray) mergReqObj.get(AzureRepoConstants.RESP_REVIEWERS);
			List<String> reviewersList = new ArrayList<>();
			if (reviewers != null) {
				for (Object reviewersObj : reviewers) {
					reviewersList.add(getString((JSONObject) reviewersObj, AzureRepoConstants.RESP_ID_KEY));
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

	private void commitDetails(List<CommitDetails> commits, String commitId, String comment, String author,
			long timestamp, ProcessorToolConnection azureRepoProcessorInfo, ProjectBasicConfig projectBasicConfig) {
		CommitDetails azureRepoCommit = new CommitDetails();
		azureRepoCommit.setBranch(azureRepoProcessorInfo.getBranch());
		azureRepoCommit.setUrl(azureRepoProcessorInfo.getUrl());
		azureRepoCommit.setTimestamp(System.currentTimeMillis());
		azureRepoCommit.setRevisionNumber(commitId);
		if (projectBasicConfig.isSaveAssigneeDetails()) {
			azureRepoCommit.setAuthor(author);
		}
		azureRepoCommit.setCommitLog(comment);

		azureRepoCommit.setCommitTimestamp(timestamp); // committer date

		commits.add(azureRepoCommit);
	}

}