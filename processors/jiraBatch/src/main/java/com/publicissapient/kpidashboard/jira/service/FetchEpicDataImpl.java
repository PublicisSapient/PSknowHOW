package com.publicissapient.kpidashboard.jira.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
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

	ProcessorJiraRestClient client;

	@Override
	public List<Issue> fetchEpic(Map.Entry<String, ProjectConfFieldMapping> entry, String boardId,
			ProcessorJiraRestClient clientIncoming, KerberosClient krb5Client) throws InterruptedException {

		ProjectConfFieldMapping projectConfig = entry.getValue();
		List<String> epicList = new ArrayList<>();
		PSLogData logData = new PSLogData();
		logData.setBoardId(boardId);
		logData.setAction(CommonConstant.EPIC_DATA);
		client = clientIncoming;
		try {
			JiraToolConfig jiraToolConfig = projectConfig.getJira();
			if (null != jiraToolConfig) {
				boolean isLast = false;
				int startIndex = 0;
				Instant start = Instant.now();
				do {
					URL url = getEpicUrl(projectConfig, boardId, startIndex);
					logData.setUrl(url.toString());
					String jsonResponse = jiraCommonService.getDataFromClient(projectConfig, url, krb5Client);
					isLast = populateData(jsonResponse, epicList);
					startIndex = epicList.size();
					TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
				} while (!isLast);
				logData.setTimeTaken(String.valueOf(Duration.between(start, Instant.now()).toMillis()));
				logData.setEpicListFetched(epicList);
				log.info("Epics fetched through board", kv(CommonConstant.PSLOGDATA, logData));
			}
		} catch (RestClientException rce) {
			log.error("Client exception when loading epic data", rce, kv(CommonConstant.PSLOGDATA, logData));
			throw rce;
		} catch (MalformedURLException mfe) {
			log.error("Malformed url for loading epic data", mfe, kv(CommonConstant.PSLOGDATA, logData));
		} catch (IOException ioe) {
			log.error("IOException", ioe, kv(CommonConstant.PSLOGDATA, logData));
		}
		return getEpicIssuesQuery(epicList, logData);
	}

	private List<Issue> getEpicIssuesQuery(List<String> epicKeyList, PSLogData logData) throws InterruptedException {

		PSLogData psLogData = new PSLogData();
		List<Issue> issueList = new ArrayList<>();
		SearchResult searchResult = null;
		try {
			if (CollectionUtils.isNotEmpty(epicKeyList)) {
				String query = "key in (" + String.join(",", epicKeyList) + ")";
				int pageStart = 0;
				int totalEpic = 0;
				int fetchedEpic = 0;
				boolean continueFlag = true;
				Instant start = Instant.now();
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
				logData.setTimeTaken(String.valueOf(Duration.between(start, Instant.now()).toMillis()));
				logData.setTotalFetchedIssues(String.valueOf(totalEpic));
				logData.setJql(query);
				log.info("Issues in epic", kv(CommonConstant.PSLOGDATA, logData));

			} else {
				log.info("No Epic Found to fetch", kv(CommonConstant.PSLOGDATA, logData));
			}
		} catch (RestClientException e) {
			log.error("Error while fetching issues", e.getCause(), kv(CommonConstant.PSLOGDATA, logData));
		}
		psLogData.setEpicIssuesFetched((issueList == null) ? "-1" : String.valueOf(issueList.size()));
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
				isLast = Boolean.valueOf(obj.get("isLast").toString());
			} catch (ParseException pe) {
				log.error("Parser exception when parsing statuses", pe);
			}
		}
		return isLast;
	}

	private void getEpic(JSONArray valuesJson, List<String> epicList) {
		valuesJson.forEach(values -> {
			JSONObject sprintJson = (JSONObject) values;
			if (null != sprintJson) {
				epicList.add(sprintJson.get(KEY).toString());
			}
		});
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
