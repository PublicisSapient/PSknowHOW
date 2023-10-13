package com.publicissapient.kpidashboard.jira.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JiraIssueMetadata {
	private Map<String, String> statusMap;
	private Map<String, String> priorityMap;
	private Map<String, String> issueTypeMap;
}
