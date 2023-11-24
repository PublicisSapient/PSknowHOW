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
package com.publicissapient.kpidashboard.jira.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FetchEpicDataImpl implements FetchEpicData {

	private static final String KEY = "key";
	@Autowired
	private JiraCommonService jiraCommonService;
	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Override
	public List<Issue> fetchEpic(ProjectConfFieldMapping projectConfig, String boardId, ProcessorJiraRestClient client,
			KerberosClient krb5Client) throws InterruptedException, IOException {

		List<String> epicList = new ArrayList<>();
		try {
			JiraToolConfig jiraToolConfig = projectConfig.getJira();
			if (null != jiraToolConfig) {
				boolean isLast = false;
				int startIndex = 0;
				do {
					URL url = getEpicUrl(projectConfig, boardId, startIndex);
					String jsonResponse = jiraCommonService.getDataFromClient(projectConfig, url, krb5Client);
					isLast = populateData(jsonResponse, epicList);
					startIndex = epicList.size();
					TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
				} while (!isLast);

			}
		} catch (RestClientException rce) {
			log.error("Client exception when loading epic data", rce);
			throw rce;
		} catch (MalformedURLException mfe) {
			log.error("Malformed url for loading epic data", mfe);
			throw mfe;
		}

		return getEpicIssuesQuery(epicList, client);
	}

	private List<Issue> getEpicIssuesQuery(List<String> epicKeyList, ProcessorJiraRestClient client)
			throws InterruptedException {

		List<Issue> issueList = new ArrayList<>();
		SearchResult searchResult = null;
		try {
			if (CollectionUtils.isNotEmpty(epicKeyList)) {
				String query = "key in (" + String.join(",", epicKeyList) + ")";
				int pageStart = 0;
				int totalEpic = 0;
				int fetchedEpic = 0;
				boolean continueFlag = true;
				do {
					Promise<SearchResult> promise = client.getSearchClient().searchJql(query,
							jiraProcessorConfig.getPageSize(), pageStart, null);
					searchResult = promise.claim();
					if (null != searchResult && null != searchResult.getIssues()) {
						if (totalEpic == 0) {
							totalEpic = searchResult.getTotal();
						}
						int issueCount = 0;
						for (Issue issue : searchResult.getIssues()) {
							issueList.add(issue);
							issueCount++;
						}
						fetchedEpic += issueCount;
						pageStart += issueCount;
						if (totalEpic <= fetchedEpic) {
							fetchedEpic = totalEpic;
							continueFlag = false;
						}
					} else {
						break;
					}
					TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
				} while (totalEpic < fetchedEpic || continueFlag);
			}
		} catch (RestClientException e) {
			log.error("Error while fetching issues", e.getCause());
			throw e;
		}
		return issueList;
	}

	private boolean populateData(String sprintReportObj, List<String> epicList) {
		boolean isLast = true;
		if (StringUtils.isNotBlank(sprintReportObj)) {
			JSONArray valuesJson = new JSONArray();
			try {
				JSONObject obj = (JSONObject) new JSONParser().parse(sprintReportObj);
				if (null != obj) {
					valuesJson = (JSONArray) obj.get("values");
				}
				getEpic(valuesJson, epicList);
				isLast = Boolean.parseBoolean(Objects.requireNonNull(obj).get("isLast").toString());
			} catch (ParseException pe) {
				log.error("Parser exception when parsing statuses", pe);
			}
		}
		return isLast;
	}

	private void getEpic(JSONArray valuesJson, List<String> epicList) {
		for (int i = 0; i < valuesJson.size(); i++) {
			JSONObject sprintJson = (JSONObject) valuesJson.get(i);
			if (null != sprintJson) {
				epicList.add(sprintJson.get(KEY).toString());
			}
		}
	}

	private URL getEpicUrl(ProjectConfFieldMapping projectConfig, String boardId, int startIndex)
			throws MalformedURLException {

		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		String serverURL = jiraProcessorConfig.getJiraEpicApi();

		serverURL = serverURL.replace("{startAtIndex}", String.valueOf(startIndex)).replace("{boardId}", boardId);
		String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
		return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + serverURL);
	}

}
