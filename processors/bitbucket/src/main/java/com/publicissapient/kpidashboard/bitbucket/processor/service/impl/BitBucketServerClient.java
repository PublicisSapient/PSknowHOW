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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.publicissapient.kpidashboard.bitbucket.config.BitBucketConfig;
import com.publicissapient.kpidashboard.bitbucket.constants.BitBucketConstants;
import com.publicissapient.kpidashboard.bitbucket.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.bitbucket.model.BitbucketRepo;
import com.publicissapient.kpidashboard.bitbucket.processor.service.BitBucketClient;
import com.publicissapient.kpidashboard.bitbucket.processor.service.impl.common.BasicBitBucketClient;
import com.publicissapient.kpidashboard.bitbucket.util.BitbucketRestOperations;
import com.publicissapient.kpidashboard.common.constant.CommitType;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BitBucketServerClient extends BasicBitBucketClient implements BitBucketClient {

	private String utfValue = "UTF-8";

	/**
	 * Instantiates a new bit bucket server client.
	 *
	 * @param config
	 *            the config
	 * @param bitbucketRestOperations
	 *            the rest operations supplier
	 * @param aesEncryptionService
	 *            the aesEncryptionService
	 */
	@Autowired
	public BitBucketServerClient(BitBucketConfig config, BitbucketRestOperations bitbucketRestOperations,
			AesEncryptionService aesEncryptionService) {
		super(config, bitbucketRestOperations, aesEncryptionService);
	}

	/**
	 * Fetch all commits.
	 *
	 * @param repo
	 *            the repo
	 * @param firstRun
	 *            the first run
	 * @param bitBucketServerInfo
	 *            the bitbucketServerInfo
	 * @return the list
	 * @throws FetchingCommitException
	 *             the exception
	 */
	@Override
	public List<CommitDetails> fetchAllCommits(BitbucketRepo repo, boolean firstRun,
			ProcessorToolConnection bitBucketServerInfo, ProjectBasicConfig proBasicConfig)
			throws FetchingCommitException {

		String restUri = null;
		List<CommitDetails> commits = new ArrayList<>();
		try {
			String decryptedPassword = decryptPassword(bitBucketServerInfo.getPassword());
			boolean isLast = false;
			String restUrl = new BitBucketServerURIBuilder(repo, config, bitBucketServerInfo).build();
			restUri = URLDecoder.decode(restUrl, utfValue);
			log.debug("REST URL {}", restUri);
			while (!isLast) {
				ResponseEntity<String> respPayload = getResponse(bitBucketServerInfo.getUsername(), decryptedPassword,
						restUri);
				JSONObject responseJson = getJSONFromResponse(respPayload.getBody());
				JSONArray jsonArray = (JSONArray) responseJson.get(BitBucketConstants.RESP_VALUES_KEY);
				String nextPageIndex = getString(responseJson, BitBucketConstants.RESP_NEXTPAGE_START);
				initializeCommitDetails(commits, jsonArray, bitBucketServerInfo, proBasicConfig);
				isLast = parseResp(jsonArray, firstRun, nextPageIndex, responseJson);
				log.info(String.format("Retrieving page : {%s}", nextPageIndex));
				if (nextPageIndex != null && !"null".equals(nextPageIndex)) {
					restUri = URLDecoder.decode(restUrl.concat("&start=").concat(nextPageIndex), utfValue);
				}
			}
			repo.setUpdatedTime(System.currentTimeMillis());
		} catch (URISyntaxException | RestClientException | ParseException | UnsupportedEncodingException ex) {
			log.error("Failed to fetch commit details ", ex);
			throw new FetchingCommitException("Failed to fetch commits", ex);
		}

		return commits;
	}

	private boolean parseResp(JSONArray jsonArray, boolean firstRun, String nextPageIndex, JSONObject responseJson) {
		boolean isLast;
		if (CollectionUtils.isEmpty(jsonArray) || (firstRun
				&& config.getInitialPageSize() <= Integer.parseInt(nextPageIndex == null ? "0" : nextPageIndex))) {
			isLast = true;
		} else {
			String lastPageValue = getString(responseJson, BitBucketConstants.RESP_IS_LASTPAGE);
			isLast = Boolean.TRUE;
			if (lastPageValue != null) {
				isLast = Boolean.valueOf(lastPageValue);
			}

		}
		return isLast;
	}

	private void initializeCommitDetails(List<CommitDetails> commits, JSONArray jsonArray,
			ProcessorToolConnection bitbucketServerInfo, ProjectBasicConfig proBasicConfig) {
		for (Object jsonObj : jsonArray) {
			JSONObject commitObject = (JSONObject) jsonObj;
			String scmRevisionNumber = getString(commitObject, BitBucketConstants.RESP_ID_KEY);
			JSONObject authorObject = (JSONObject) commitObject.get(BitBucketConstants.RESP_AUTHOR_KEY);
			String message = getString(commitObject, BitBucketConstants.RESP_MESSAGE_KEY);
			String author = getString(authorObject, BitBucketConstants.RESP_NAME_KEY);
			long timestamp = Long.parseLong(getString(commitObject, BitBucketConstants.RESP_AUTHOR_TIMESTAMP_KEY));
			JSONArray parents = (JSONArray) commitObject.get(BitBucketConstants.RESP_PARENTS_KEY);
			List<String> parentList = new ArrayList<>();
			if (parents != null) {
				for (Object parentObj : parents) {
					parentList.add(getString((JSONObject) parentObj, BitBucketConstants.RESP_ID_KEY));
				}
			}
			commitDetails(commits, scmRevisionNumber, message, author, timestamp, parentList, bitbucketServerInfo,
					proBasicConfig);

		}
	}

	@SuppressWarnings("java:S107")
	private void commitDetails(List<CommitDetails> commits, String scmRevisionNumber, String message, String author,
			long timestamp, List<String> parentList, ProcessorToolConnection bitbucketServerInfo,
			ProjectBasicConfig proBasicConfig) {
		CommitDetails bitBucketCommit = new CommitDetails();
		bitBucketCommit.setBranch(bitbucketServerInfo.getBranch());
		bitBucketCommit.setUrl(bitbucketServerInfo.getUrl());
		bitBucketCommit.setRepoSlug(bitbucketServerInfo.getRepoSlug());
		bitBucketCommit.setTimestamp(System.currentTimeMillis());
		bitBucketCommit.setRevisionNumber(scmRevisionNumber);
		if (proBasicConfig.isSaveAssigneeDetails()) {
			bitBucketCommit.setAuthor(author);
		}
		bitBucketCommit.setCommitLog(message);
		bitBucketCommit.setParentRevisionNumbers(parentList);
		bitBucketCommit.setCommitTimestamp(timestamp);
		bitBucketCommit.setType(parentList.size() > 1 ? CommitType.MERGE : CommitType.NEW);
		commits.add(bitBucketCommit);
	}

	@Override
	public List<MergeRequests> fetchMergeRequests(BitbucketRepo repo, boolean firstRun,
			ProcessorToolConnection bitBucketServerInfo, ProjectBasicConfig proBasicConfig)
			throws FetchingCommitException {

		List<MergeRequests> mergeRequests = new ArrayList<>();
		try {
			boolean isLastPage = false;
			String decryptedPassword = decryptPassword(bitBucketServerInfo.getPassword());
			String restUrl = new BitBucketServerURIBuilder(repo, config, bitBucketServerInfo).buildMergeReqURL();
			long start = 0;
			while (!isLastPage) {
				ResponseEntity<String> respPayload = getResponse(bitBucketServerInfo.getUsername(), decryptedPassword,
						URLDecoder.decode(addPaginationInfo(restUrl, start), utfValue));
				JSONObject responseJson = getJSONFromResponse(respPayload.getBody());
				JSONArray jsonArray = (JSONArray) responseJson.get(BitBucketConstants.RESP_VALUES_KEY);
				isLastPage = (boolean) responseJson.get(BitBucketConstants.RESP_IS_LASTPAGE);
				if (!isLastPage) {
					start = (long) responseJson.get(BitBucketConstants.RESP_NEXTPAGE_START);
				}
				initializeMergeRequests(mergeRequests, jsonArray, proBasicConfig);
			}
			repo.setUpdatedTime(System.currentTimeMillis());
		} catch (URISyntaxException | RestClientException | ParseException | UnsupportedEncodingException ex) {
			log.error("Failed to fetch merge requests ", ex);
			throw new FetchingCommitException("Failed to fetch merge requests", ex);
		}
		return mergeRequests;
	}

	private String addPaginationInfo(String url, long start) {
		return url + "&start=" + start;
	}

	/**
	 * @param mergeRequests
	 * @param jsonArray
	 */
	private void initializeMergeRequests(List<MergeRequests> mergeRequests, JSONArray jsonArray,
			ProjectBasicConfig proBasicConfig) {
		for (Object jsonObj : jsonArray) {
			long closedDate = 0;
			JSONObject mergReqObj = (JSONObject) jsonObj;
			String title = getString(mergReqObj, BitBucketConstants.RESP_TITLE);
			String state = getString(mergReqObj, BitBucketConstants.RESP_STATE);
			boolean isOpen = Boolean.parseBoolean(getString(mergReqObj, BitBucketConstants.RESP_OPEN));
			boolean isClosed = Boolean.parseBoolean(getString(mergReqObj, BitBucketConstants.RESP_CLOSED));
			long createdDate = Long.parseLong(getString(mergReqObj, BitBucketConstants.RESP_CREATED_DATE));
			long updatedDate = Long.parseLong(getString(mergReqObj, BitBucketConstants.RESP_UPDATED_DATE));
			if (getString(mergReqObj, BitBucketConstants.RESP_CLOSED_DATE) != null) {
				closedDate = Long.parseLong(getString(mergReqObj, BitBucketConstants.RESP_CLOSED_DATE));
			}
			JSONObject fromRefObj = (JSONObject) mergReqObj.get(BitBucketConstants.RESP_FROM_REF);
			String fromBranch = getString(fromRefObj, BitBucketConstants.RESP_DISP_ID);
			JSONObject toRefObj = (JSONObject) mergReqObj.get(BitBucketConstants.RESP_TO_REF);
			String toBranch = getString(toRefObj, BitBucketConstants.RESP_DISP_ID);
			JSONObject repoObj = (JSONObject) fromRefObj.get(BitBucketConstants.RESP_REPO);
			String repoSlug = getString(repoObj, BitBucketConstants.RESP_SLUG);
			JSONObject projObj = (JSONObject) repoObj.get(BitBucketConstants.RESP_PROJ);
			String projKey = getString(projObj, BitBucketConstants.RESP_PROJ_KEY);
			JSONObject authorObj = (JSONObject) mergReqObj.get(BitBucketConstants.RESP_AUTHOR_KEY);
			JSONObject userObj = (JSONObject) authorObj.get(BitBucketConstants.RESP_USER);
			String author = getString(userObj, BitBucketConstants.RESP_NAME_KEY);
			String scmRevisionNumber = getString(mergReqObj, BitBucketConstants.RESP_ID_KEY);
			JSONArray reviewers = (JSONArray) mergReqObj.get(BitBucketConstants.RESP_REVIEWERS);
			List<String> reviewersList = new ArrayList<>();
			if (reviewers != null) {
				for (Object reviewersObj : reviewers) {
					reviewersList.add(getString((JSONObject) reviewersObj, BitBucketConstants.RESP_ID_KEY));
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
			mergeRequests.add(mergeReq);
		}
	}

}
