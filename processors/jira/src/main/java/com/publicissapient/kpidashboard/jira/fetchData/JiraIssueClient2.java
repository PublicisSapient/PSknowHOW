package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Map;

@Slf4j
public class JiraIssueClient2 {

    protected static final String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";

    public String getFieldValue(String customFieldId, Map<String, IssueField> fields) {
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

}
