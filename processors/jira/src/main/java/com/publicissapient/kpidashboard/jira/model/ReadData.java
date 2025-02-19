package com.publicissapient.kpidashboard.jira.model;

import org.bson.types.ObjectId;

import com.atlassian.jira.rest.client.api.domain.Issue;

import lombok.Data;

@Data
public class ReadData {
	private Issue issue;
	private ProjectConfFieldMapping projectConfFieldMapping;
	private String boardId;
	private boolean isSprintFetch;
	private ObjectId processorId;
}
