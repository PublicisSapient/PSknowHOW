package com.publicissapient.kpidashboard.jira.model;

import com.atlassian.jira.rest.client.api.domain.Issue;

import lombok.Data;

@Data
public class ReadData {
	private Issue issue;
	private ProjectConfFieldMapping projectConfFieldMapping;
	private String boardId;
	private boolean isSprintFetch;

}
