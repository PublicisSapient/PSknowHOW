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

package com.publicissapient.kpidashboard.teamcity.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.application.RepoBranch;
import com.publicissapient.kpidashboard.teamcity.config.Constants;
import com.publicissapient.kpidashboard.teamcity.config.TeamcityConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides utility methods for Teamcity processor.
 */
@Component
@Slf4j
public final class ProcessorUtils {// NOPMD

	private static final String TRIGGERED = "triggered";

	private ProcessorUtils() {
	}

	/**
	 * Builds Job Query as a string.
	 * 
	 * @param config
	 *            the teamcity configuration details
	 * @param jobQuery
	 *            the job query data
	 * @return the build job query
	 */
	public static String buildJobQueryString(TeamcityConfig config, String jobQuery) {
		StringBuilder query = new StringBuilder(jobQuery);
		int depth = config.getFolderDepth();
		for (int i = 1; i < depth; i++) {
			query.insert((query.length() - i), ",");
			query.insert((query.length() - i), jobQuery.substring(0, jobQuery.length() - 1));
			query.insert((query.length() - i), "]");
		}
		return query.toString();
	}

	/**
	 * Provides Domain name.
	 * 
	 * @param url
	 *            the URL
	 * @return the domain name
	 * @throws URISyntaxException
	 *             if there is any illegal character in URI
	 */
	public static String extractDomain(String url) throws URISyntaxException {
		URI uri = new URI(url);
		return uri.getHost();
	}

	/**
	 * Provides Port number.
	 * 
	 * @param url
	 *            the URL
	 * @return port the port number
	 * @throws URISyntaxException
	 *             if there is any illegal character in URI
	 */
	public static int extractPort(String url) throws URISyntaxException {
		URI uri = new URI(url);
		return uri.getPort();
	}

	/**
	 * Creates HTTP Headers.
	 * 
	 * @param userInfo
	 *            the user info
	 * @return the HttpHeaders
	 */
	public static HttpHeaders createHeaders(final String userInfo) {
		byte[] encodedAuth = Base64.getEncoder().encode(userInfo.getBytes(StandardCharsets.US_ASCII));
		String authHeader = "Basic " + new String(encodedAuth);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, authHeader);
		headers.set(HttpHeaders.ACCEPT, Constants.FORMAT);
		headers.set(HttpHeaders.CONTENT_TYPE, Constants.FORMAT);

		return headers;
	}

	/**
	 * Join URL.
	 * 
	 * @param base
	 *            the base
	 * @param paths
	 *            the path
	 * @return the join URL
	 */
	public static String joinURL(String base, String... paths) {
		StringBuilder result = new StringBuilder(base);
		for (String path : paths) {
			String tmpPath = path.replaceFirst("^(\\/)+", "");
			if (result.lastIndexOf("/") != result.length() - 1) {
				result.append('/');
			}
			result.append(tmpPath);
		}
		return result.toString();
	}

	/**
	 * Gathers repo urls, and the branch name from the last built revision. Filters
	 * out the qualifiers from the branch name and sets the unqualified branch name.
	 * 
	 * @param buildJson
	 *            the build JSON
	 * @return the list of repository branch
	 */
	public static List<RepoBranch> getGitRepoBranch(JSONObject buildJson) {
		List<RepoBranch> list = new ArrayList<>();

		JSONArray actions = getJsonArray(buildJson, "actions");
		for (Object action : actions) {
			JSONObject jsonAction = (JSONObject) action;
			if (jsonAction.size() > 0) {
				JSONObject lastBuiltRevision = null;
				JSONArray branches = null;
				JSONArray remoteUrls = getJsonArray((JSONObject) action, "remoteUrls");
				if (!remoteUrls.isEmpty()) {
					lastBuiltRevision = (JSONObject) jsonAction.get("lastBuiltRevision");
				}
				if (lastBuiltRevision != null) {
					branches = getJsonArray(lastBuiltRevision, "branch");
				}

				addRepoBranchesToList(list, branches, remoteUrls);
			}
		}
		return list;
	}

	/**
	 * add repo branches to the list
	 * 
	 * @param list
	 *            the list
	 * @param branches
	 *            branches
	 * @param remoteUrls
	 *            remote url
	 */
	private static void addRepoBranchesToList(List<RepoBranch> list, JSONArray branches, JSONArray remoteUrls) {
		if (CollectionUtils.isEmpty(branches)) {
			return;
		}
		for (Object url : remoteUrls) {
			String sUrl = (String) url;
			if (sUrl != null && !sUrl.isEmpty()) {
				sUrl = removeGitExtensionFromUrl(sUrl);
				for (Object branchObj : branches) {
					String branchName = getString((JSONObject) branchObj, "name");
					RepoBranch repoBranch = createRepoBranch(branchName, sUrl);
					if (repoBranch != null) {
						list.add(repoBranch);
					}

				}
			}
		}

	}

	/**
	 * Gets RepoBranch
	 * 
	 * @param branchName
	 *            the branch name
	 * @param url
	 *            the url
	 * @return RepoBranch object if branch name is not null
	 */
	private static RepoBranch createRepoBranch(String branchName, String url) {
		if (branchName != null) {
			String unqualifiedBranchName = getUnqualifiedBranch(branchName);
			return new RepoBranch(url, unqualifiedBranchName, RepoBranch.RepoType.GIT);

		}

		return null;

	}

	/**
	 * Removes Git Extension from URL.
	 * 
	 * @param url
	 *            the URL
	 * @return sUrl the rest call URL
	 */
	public static String removeGitExtensionFromUrl(String url) {
		String sUrl = url;
		// remove .git from the urls
		if (sUrl.endsWith(".git")) {
			sUrl = sUrl.substring(0, sUrl.lastIndexOf(".git"));
		}
		return sUrl;
	}

	/**
	 * Provides the unqualified branch name.
	 * 
	 * @param qualifiedBranch
	 *            the full name of branch
	 * @return the unqualified branch name
	 */
	public static String getUnqualifiedBranch(String qualifiedBranch) {
		String branchName = qualifiedBranch;
		Pattern pattern = Pattern.compile("(refs/)?remotes/[^/]+/(.*)|(origin[0-9]*/)?(.*)");
		Matcher matcher = pattern.matcher(branchName);
		if (matcher.matches()) {
			if (matcher.group(2) != null) {
				branchName = matcher.group(2);
			} else if (matcher.group(4) != null) {
				branchName = matcher.group(4);
			}
		}
		return branchName;
	}

	/**
	 * Converts JSONObject to String.
	 * 
	 * @param json
	 *            the json object
	 * @param key
	 *            the key
	 * @return the string data
	 */
	public static String getString(JSONObject json, String key) {
		return (String) json.get(key);
	}

	/**
	 * Provides JsonArray.
	 * 
	 * @param json
	 *            the json
	 * @param key
	 *            the key
	 * @return the JSONArray
	 */
	public static JSONArray getJsonArray(JSONObject json, String key) {
		Object array = json.get(key);
		return array == null ? new JSONArray() : (JSONArray) array;
	}

	/**
	 * Provides first culprit.
	 *
	 * @param buildJson
	 *            the build json
	 * @return the json string
	 */
	public static String firstCulprit(JSONObject buildJson) {
		JSONObject triggered = (JSONObject) buildJson.get(TRIGGERED);
		JSONObject lastChanged = (JSONObject) buildJson.get("lastChanges");
		String culpritName = StringUtils.EMPTY;
		if (triggered != null) {
			JSONObject user = (JSONObject) triggered.get("user");
			if (user != null) {
				culpritName = user.get("name").toString();
			}

		}
		if (lastChanged != null) {
			JSONArray changedArray = (JSONArray) lastChanged.get("change");
			if (!changedArray.isEmpty()) {
				JSONObject culprit = (JSONObject) changedArray.get(0);
				culpritName = culprit.get("username").toString();
			}
		}

		return culpritName;

	}

	public static String startDate(JSONObject buildJson) {
		JSONArray culprits = getJsonArray(buildJson, "build");
		if (CollectionUtils.isEmpty(culprits)) {
			return null;
		}
		JSONObject culprit = (JSONObject) culprits.get(0);
		return getFullName(culprit);
	}

	/**
	 * Provides Full Name.
	 *
	 * @param jsonObject
	 *            the json object
	 * @return the json data as string
	 */
	public static String getFullName(JSONObject jsonObject) {
		JSONObject triggered = (JSONObject) jsonObject.get(TRIGGERED);
		return getString(triggered, "user");
	}

	public static String getStartDate(JSONObject jsonObject) {
		JSONObject triggered = (JSONObject) jsonObject.get(TRIGGERED);
		return getString(triggered, "date");
	}

	/**
	 * Provides Commit Author.
	 *
	 * @param jsonItem
	 *            the json item
	 * @return the commit author
	 */
	public static String getCommitAuthor(JSONObject jsonItem) {
		// Use user if provided, otherwise use author.fullName
		JSONObject author = (JSONObject) jsonItem.get("author");
		return author == null ? getString(jsonItem, "user") : getFullName(author);
	}

	/**
	 * Provides commit Timestamp.
	 *
	 * @param dateString
	 *            the josn item
	 *
	 * @return the timestamp
	 */
	public static long getCommitTimestamp(String dateString) {
		String parseableDate = dateString.substring(0, 4) + '-' + dateString.substring(4, 6) + "-"
				+ dateString.substring(6, 8) + "T" + dateString.substring(9, 11) + ":" + dateString.substring(11, 13)
				+ ":" + dateString.substring(13, 15);

		try {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(parseableDate).getTime();
		} catch (java.text.ParseException e) {
			log.error(String.format("Invalid date string: %s", parseableDate), e);
		}

		return 0;
	}

	/**
	 * Checks if both urls are have same domain name and port number
	 * 
	 * @param url1
	 *            first url
	 * @param url2
	 *            second url
	 * @return true if both domain and port is the same
	 */
	public static boolean isSameServerInfo(String url1, String url2) {

		try {
			String domain1 = ProcessorUtils.extractDomain(url1);
			int port1 = ProcessorUtils.extractPort(url1);
			String domain2 = ProcessorUtils.extractDomain(url2);
			int port2 = ProcessorUtils.extractPort(url2);

			if (StringUtils.isEmpty(domain1) || StringUtils.isEmpty(domain2)) {
				return false;
			}
			if (domain1.equals(domain2) && port1 == port2) {
				return true;
			}

		} catch (URISyntaxException exception) {
			log.error(String.format("uri syntax error url1= %s, url2 = %s", url1, url2), exception);
		}

		return false;
	}

}
