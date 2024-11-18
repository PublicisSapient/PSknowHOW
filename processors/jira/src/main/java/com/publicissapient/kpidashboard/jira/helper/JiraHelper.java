package com.publicissapient.kpidashboard.jira.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JiraHelper {

	public static final Comparator<SprintDetails> SPRINT_COMPARATOR = (SprintDetails o1, SprintDetails o2) -> {
		int cmp1 = ObjectUtils.compare(o1.getStartDate(), o2.getStartDate());
		if (cmp1 != 0) {
			return cmp1;
		}
		return ObjectUtils.compare(o1.getEndDate(), o2.getEndDate());
	};
	private static final String ERROR_MSG_401 = "Error 401 connecting to JIRA server, your credentials are probably wrong. Note: Ensure you are using JIRA user name not your email address.";
	private static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Jira unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";
	private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";

	public static Map<String, IssueField> buildFieldMap(Iterable<IssueField> fields) {
		Map<String, IssueField> rt = new HashMap<>();

		if (fields != null) {
			for (IssueField issueField : fields) {
				rt.put(issueField.getId(), issueField);
			}
		}

		return rt;
	}

	public static List<String> getLabelsList(Issue issue) {
		List<String> labels = new ArrayList<>();
		if (issue.getLabels() != null) {
			for (String labelName : issue.getLabels()) {
				labels.add(JiraProcessorUtil.deodeUTF8String(labelName));
			}
		}
		return labels;
	}

	public static List<String> getAffectedVersions(Issue issue) {
		List<String> affectedVersions = new ArrayList<>();
		if (issue.getAffectedVersions() != null) {
			for (Version affectedVersionName : issue.getAffectedVersions()) {
				affectedVersions.add(affectedVersionName.getName());
			}
		}
		return affectedVersions;
	}

	public static String getFieldValue(String customFieldId, Map<String, IssueField> fields) {
		Object fieldValue = fields.get(customFieldId).getValue();
		try {
			if (fieldValue instanceof Double) {
				return fieldValue.toString();
			} else if (fieldValue instanceof JSONObject) {
				return ((JSONObject) fieldValue).getString(JiraConstants.VALUE);
			} else if (fieldValue instanceof String) {
				return fieldValue.toString();
			}
		} catch (JSONException e) {
			log.error("JIRA Processor | Error while parsing RCA Custom_Field", e);
		}
		return fieldValue.toString();
	}

	public static List<ChangelogGroup> sortChangeLogGroup(Issue issue) {
		Iterable<ChangelogGroup> changelogItr = issue.getChangelog();
		List<ChangelogGroup> changeLogList = new ArrayList<>();
		if (null != changelogItr) {
			changeLogList = Lists.newArrayList(changelogItr.iterator());
			changeLogList.sort((ChangelogGroup obj1, ChangelogGroup obj2) -> {
				DateTime activityDate1 = obj1.getCreated();
				DateTime activityDate2 = obj2.getCreated();
				return activityDate1.compareTo(activityDate2);
			});
		}
		return changeLogList;
	}

	public static List<Issue> getIssuesFromResult(SearchResult searchResult) {
		if (searchResult != null) {
			return Lists.newArrayList(searchResult.getIssues());
		}
		return new ArrayList<>();
	}

	public static String hash(String input) {
		return String.valueOf(Objects.hash(input));
	}

	public static String getAssignee(User user) {
		String userId = "";
		String query = user.getSelf().getQuery();
		if (StringUtils.isNotEmpty(query) && (query.contains("accountId") || query.contains("username"))) {
			userId = query.split("=")[1];
		}
		return userId;
	}

	public static Collection getListFromJson(IssueField issueField) {

		Object value = issueField.getValue();
		final List list = new ArrayList<>();
		if (value instanceof JSONArray) {

			((JSONArray) value).forEach(v -> {
				try {
					list.add(((JSONObject) v).get(JiraConstants.VALUE));
				} catch (JSONException e) {
					log.error("JIRA PROCESSOR | Error while parsing Atlassian Issue JSON Object", e);
				}
			});
		} else if (value instanceof JSONObject) {
			try {
				list.add(((JSONObject) value).get(JiraConstants.VALUE));
			} catch (JSONException e) {
				log.error("JIRA PROCESSOR | Error while parsing Atlassian Issue JSON Object", e);
			}
		}
		return list;
	}

	public static List<Status> getStatus(ProcessorJiraRestClient client) {
		List<Status> statusList = new ArrayList<>();

		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			try {
				Promise<Iterable<Status>> promisedRs = client.getMetadataClient().getStatuses();

				Iterable<Status> fieldIt = promisedRs.claim();
				if (fieldIt != null) {
					statusList = Lists.newArrayList(fieldIt.iterator());
				}
			} catch (RestClientException e) {
				exceptionBlockProcess(e);
				throw e;
			}
		}

		return statusList;
	}

	public static void exceptionBlockProcess(RestClientException e) {
		if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
			log.error(ERROR_MSG_401);
		} else {
			log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e.getCause());
		}
	}

	public static String convertDateToCustomFormat(long currentTimeMillis) {
		Date inputDate = new Date(currentTimeMillis);
		SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy, EEEE, hh:mm:ss a");

		String outputStr = outputFormat.format(inputDate);

		return outputStr;
	}

}
