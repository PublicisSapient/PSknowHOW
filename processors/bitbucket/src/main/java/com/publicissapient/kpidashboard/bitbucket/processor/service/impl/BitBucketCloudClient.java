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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.MDC;
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
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

import lombok.extern.slf4j.Slf4j;

/**
 * BitBucketCloudClient represents a class which holds all the configuration and
 * BitBucket execution process.
 *
 * @see BasicBitBucketClient
 */
@Slf4j
@Component
public class BitBucketCloudClient extends BasicBitBucketClient implements BitBucketClient {

	/**
	 * Instantiates a new bit bucket cloud client.
	 *
	 * @param settings                the settings
	 * @param bitbucketRestOperations the rest operations supplier
	 * @param aesEncryptionService    the aesEncryptionService
	 */
	@Autowired
	public BitBucketCloudClient(BitBucketConfig settings, BitbucketRestOperations bitbucketRestOperations,
			AesEncryptionService aesEncryptionService) {
		super(settings, bitbucketRestOperations, aesEncryptionService);
	}

	/**
	 * Fetch all commits.
	 *
	 * @param bitbucketRepo        the bitbucketRepo
	 * @param initialRunOccurrence the initialRunOccurrence
	 * @param bitBucketServerInfo  the connection details
	 * @return the list
	 * @throws FetchingCommitException the exception
	 */
	@Override
	public List<CommitDetails> fetchAllCommits(BitbucketRepo bitbucketRepo, boolean initialRunOccurrence,
			ProcessorToolConnection bitBucketServerInfo,ProjectBasicConfig proBasicConfig) throws FetchingCommitException {
		List<CommitDetails> commits = new ArrayList<>();
		try {
			String restUrl = new BitBucketCloudURIBuilder(bitbucketRepo, config, bitBucketServerInfo).build();
			boolean last = false;
			int page = 1;
			String cloneUrl = URLDecoder.decode(restUrl, "UTF-8");
			long sinceTime = new DateTime().minusDays(config.getSinceDaysCloud()).getMillis();
			while (!last) {
				String plainTxtPassword = decryptPassword(bitBucketServerInfo.getPassword());
				ResponseEntity<String> response = getResponse(bitBucketServerInfo.getUsername(), plainTxtPassword,
						cloneUrl);
				JSONObject jsonParentObject = getJSONFromResponse(response.getBody());
				JSONArray jsonArray = (JSONArray) jsonParentObject.get(BitBucketConstants.RESP_VALUES_KEY);

				if (CollectionUtils.isNotEmpty(jsonArray)) {
					Object jsonNextObject = jsonParentObject.get(BitBucketConstants.NEXT);
					initializeCommitDetails(commits, jsonArray, bitBucketServerInfo, proBasicConfig);
					long commitTimePageWise = commits.get(commits.size() - 1).getCommitTimestamp();
					last = isLastPage(jsonNextObject, commitTimePageWise, sinceTime);
					log.info("commit data collected for page : ", page);
					cloneUrl = URLDecoder.decode(restUrl.concat("&page=" + (++page)), "UTF-8");
					
				} else {
					last = true;
				}
				log.info("fetching cloud commits - last page = " + last);
			}
		} catch (URISyntaxException | RestClientException | ParseException | UnsupportedEncodingException ex) {
			log.error("Failed to fetch commit details ", ex);
			throw new FetchingCommitException("Failed to fetch commits", ex);
		}
		return commits;
	}

	private void initializeCommitDetails(List<CommitDetails> commits, JSONArray jsonArray,
			ProcessorToolConnection bitBucketServerInfo,ProjectBasicConfig proBasicConfig) {
		for (Object jsonItem : jsonArray) {
			JSONObject commitObj = (JSONObject) jsonItem;
			String hash = getString(commitObj, BitBucketConstants.RESP_HASH_KEY);
			JSONObject authorObject = (JSONObject) commitObj.get(BitBucketConstants.RESP_AUTHOR_KEY);
			String message = getString(commitObj, BitBucketConstants.RESP_MESSAGE_KEY);
			String author = getString(authorObject, BitBucketConstants.RESP_RAW_KEY);
			long timestamp = new DateTime(getString(commitObj, BitBucketConstants.RESP_DATE_KEY)).getMillis();
			JSONArray parents = (JSONArray) commitObj.get(BitBucketConstants.RESP_PARENTS_KEY);
			List<String> parentList = new ArrayList<>(parents.size());
			for (Object parentObj : parents) {
				parentList.add(getString((JSONObject) parentObj, BitBucketConstants.RESP_ID_KEY));
			}
			commitDetails(commits, hash, message, author, timestamp, parentList, bitBucketServerInfo, proBasicConfig);
		}
	}

	private void commitDetails(List<CommitDetails> commits, String hash, String message, String author, long timestamp,
			List<String> parentList, ProcessorToolConnection bitBucketServerInfo,ProjectBasicConfig proBasicConfig) {
		CommitDetails bitbucketCommit = new CommitDetails();
		bitbucketCommit.setBranch(bitBucketServerInfo.getBranch());
		bitbucketCommit.setUrl(bitBucketServerInfo.getUrl());
		bitbucketCommit.setTimestamp(System.currentTimeMillis());
		bitbucketCommit.setCommitLog(message);
		if(proBasicConfig.isSaveAssigneeDetails()) {
			bitbucketCommit.setAuthor(author);
		}
		bitbucketCommit.setRevisionNumber(hash);
		bitbucketCommit.setParentRevisionNumbers(parentList);
		bitbucketCommit.setCommitTimestamp(timestamp);
		bitbucketCommit.setType(parentList.size() > 1 ? CommitType.MERGE : CommitType.NEW);
		bitbucketCommit.setRepoSlug(bitBucketServerInfo.getRepoSlug());
		commits.add(bitbucketCommit);
	}

	/**
	 * Checks if is last page.
	 *
	 *
	 * @return true, if is last page
	 */
	private boolean isLastPage(Object next, long commitTimePageWise, long sinceTime) {
		boolean isLast = true;
		if (null != next && commitTimePageWise >= sinceTime) {
			isLast = false;
		}
		return isLast;
	}

	/**
	 * Gets the past date.
	 *
	 * @param repo     the repo
	 * @param firstRun the first run
	 * @param histDays the hist days
	 * @return the past date
	 */
	private String getPastDate(BitbucketRepo repo, boolean firstRun, int histDays) {
		Date pastDate;
		int theHistDays = histDays;
		if (firstRun) {
			theHistDays = (theHistDays > 0) ? theHistDays : BitBucketConstants.FIRST_RUN_HISTORY_DEFAULT;
			pastDate = getDate(new Date(), -theHistDays, 0);
		} else {
			pastDate = getDate(repo.getLastUpdatedTime(), 0, -10);
		}
		Calendar calendar = Calendar.getInstance(new GregorianCalendar().getTimeZone());
		calendar.setTime(pastDate);
		return String.format("%tFT%<tRZ", calendar);
	}

	/**
	 * Builds the api url.
	 *
	 * @param theRepoUrl  the the repo url
	 * @param defaultHost the default host
	 * @param defaultApi  the default api
	 * @return the string
	 */
	private String buildEndPointUrl(String theRepoUrl, String defaultHost, String defaultApi) {
		String repoUrl = StringUtils.removeEnd(theRepoUrl, ".git");
		String hostName = "";
		String protocol = "";
		String apiUrl = "";
		String repoName = "";

		int port = -1;

		URL url = null;
		try {
			url = new URL(repoUrl);
			protocol = url.getProtocol();
			hostName = url.getHost();
			port = url.getPort();
			repoName = url.getFile();
		} catch (MalformedURLException ex) {
			MDC.put("MalformedURLException", ex.getMessage());
		}

		if (port >= 0) {
			hostName = hostName.concat(":").concat(String.valueOf(port));
		}

		if (hostName.startsWith(defaultHost)) {
			apiUrl = concatenate(protocol, "://", defaultHost, repoName);
		} else {
			apiUrl = concatenate(protocol, "://", hostName, defaultApi, repoName);
			MDC.put("APIURL", apiUrl);
		}
		return apiUrl;
	}

	/**
	 * Concatenate.
	 *
	 * @param tokens the tokens
	 * @return the string
	 */
	private static String concatenate(String... tokens) {
		StringBuilder sb = new StringBuilder();

		if (tokens != null) {
			for (String token : tokens) {
				sb.append(token);
			}
		}

		return sb.toString();
	}

	/**
	 * Gets the date.
	 *
	 * @param date         the date
	 * @param daysOffset   the days offset
	 * @param minuteOffset the minute offset
	 * @return the date
	 */
	private Date getDate(Date date, int daysOffset, int minuteOffset) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, daysOffset);
		calendar.add(Calendar.MINUTE, minuteOffset);
		return calendar.getTime();
	}

	@Override
	public List<MergeRequests> fetchMergeRequests(BitbucketRepo repo, boolean firstRun,
			ProcessorToolConnection bitBucketServerInfo,ProjectBasicConfig proBasicConfig) throws FetchingCommitException {
		String restUri = null;
		List<MergeRequests> mergeRequests = new ArrayList<>();
		try {
			boolean isLast = false;
			int page = 1;
			String decryptedPassword = decryptPassword(bitBucketServerInfo.getPassword());
			String restUrl = new BitBucketCloudURIBuilder(repo, config, bitBucketServerInfo).buildMergeReqURL();

			log.debug("REST URL {}", restUri);
			long sinceTime = new DateTime().minusDays(config.getSinceDaysMergedCloud()).getMillis();
			while (!isLast) {
				ResponseEntity<String> respPayload = getResponse(bitBucketServerInfo.getUsername(), decryptedPassword,
						URLDecoder.decode(addPaginationInfo(restUrl, page), "UTF-8"));
				JSONObject responseJson = getJSONFromResponse(respPayload.getBody());
				Object jsonNextObject = responseJson.get(BitBucketConstants.NEXT);
				JSONArray jsonArray = (JSONArray) responseJson.get(BitBucketConstants.RESP_VALUES_KEY);
				initializeMergeRequests(mergeRequests, jsonArray, proBasicConfig);
				long mergedTimePageWise = mergeRequests.get(mergeRequests.size() - 1).getUpdatedDate();
				isLast = isLastPage(jsonNextObject, mergedTimePageWise, sinceTime);
				log.info("Merged data collected for page : ", page);
				page++;
			}
			repo.setUpdatedTime(System.currentTimeMillis());
		} catch (URISyntaxException | RestClientException | ParseException | UnsupportedEncodingException ex) {
			log.error("Failed to fetch merge requests ", ex);
			throw new FetchingCommitException("Failed to fetch merge requests", ex);
		}
		return mergeRequests;
	}

	private String addPaginationInfo(String url, int page){
		return url + "&page=" + page;
	}

	/**
	 * @param mergeRequests
	 * @param jsonArray
	 */
	private void initializeMergeRequests(List<MergeRequests> mergeRequests, JSONArray jsonArray,ProjectBasicConfig proBasicConfig) {
		for (Object jsonObj : jsonArray) {
			long closedDate = 0;
			JSONObject mergReqObj = (JSONObject) jsonObj;
			String title = getString(mergReqObj, BitBucketConstants.RESP_TITLE);
			String state = getString(mergReqObj, BitBucketConstants.RESP_STATE);
			boolean isOpen = StringUtils.isNotBlank(state) && state.equalsIgnoreCase(BitBucketConstants.RESP_OPEN)
					? Boolean.TRUE
					: Boolean.FALSE;
			boolean isClosed = StringUtils.isNotBlank(state) && state.equalsIgnoreCase(BitBucketConstants.RESP_MERGED)
					? Boolean.TRUE
					: Boolean.FALSE;
			String createdDateStr = getString(mergReqObj, BitBucketConstants.RESP_CREATED_ON);
			long createdDate = StringUtils.isNotBlank(createdDateStr) ? new DateTime(createdDateStr).getMillis() : 0;
			String updatedDateStr = getString(mergReqObj, BitBucketConstants.RESP_UPDATED_ON);
			long updatedDate = StringUtils.isNotBlank(updatedDateStr) ? new DateTime(updatedDateStr).getMillis() : 0;
			if (StringUtils.isNotBlank(state) && state.equalsIgnoreCase(BitBucketConstants.RESP_MERGED)) {
				closedDate = updatedDate;
			}
			JSONObject fromRefObj = (JSONObject) mergReqObj.get(BitBucketConstants.RESP_SOURCE);
			JSONObject fromBranchObj = (JSONObject) (fromRefObj.get(BitBucketConstants.RESP_BRANCH));
			String fromBranch = getString(fromBranchObj, BitBucketConstants.RESP_BRANCH_NAME);

			JSONObject toRefObj = (JSONObject) mergReqObj.get(BitBucketConstants.RESP_DESTINATION);
			JSONObject toBranchObj = (JSONObject) (toRefObj.get(BitBucketConstants.RESP_BRANCH));
			String toBranch = getString(toBranchObj, BitBucketConstants.RESP_BRANCH_NAME);

			JSONObject repoObj = (JSONObject) fromRefObj.get(BitBucketConstants.RESP_REPO);
			String repoSlug = getString(repoObj, BitBucketConstants.RESP_REPO_NAME);

			JSONObject authorObj = (JSONObject) mergReqObj.get(BitBucketConstants.RESP_AUTHOR_KEY);
			String author = getString(authorObj, BitBucketConstants.RESP_DISPLAY_NAME);
			String scmRevisionNumber = getString(mergReqObj, BitBucketConstants.RESP_ID_KEY);

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
			if(proBasicConfig.isSaveAssigneeDetails()) {
				mergeReq.setAuthor(author);
			}
			mergeReq.setRevisionNumber(scmRevisionNumber);
			mergeRequests.add(mergeReq);
		}
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


}
