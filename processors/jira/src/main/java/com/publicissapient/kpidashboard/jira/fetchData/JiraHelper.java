package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.*;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.*;

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

    public static List<String> getAffectedVersions(Issue issue){
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
        if(null != changelogItr) {
            changeLogList = Lists.newArrayList(changelogItr.iterator());
            changeLogList.sort((ChangelogGroup obj1, ChangelogGroup obj2) -> {
                DateTime activityDate1 = obj1.getCreated();
                DateTime activityDate2 = obj2.getCreated();
                return activityDate1.compareTo(activityDate2);
            });
        }
        return changeLogList;
    }

    static int getTotal(SearchResult searchResult) {
        if (searchResult != null) {
            return searchResult.getTotal();
        }
        return 0;
    }

   static List<Issue> getIssuesFromResult(SearchResult searchResult) {
        if (searchResult != null) {
            return Lists.newArrayList(searchResult.getIssues());
        }
        return new ArrayList<>();
    }


}
