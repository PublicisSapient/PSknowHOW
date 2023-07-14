package com.publicissapient.kpidashboard.apis.jira.model;

import lombok.Data;

@Data
public class BoardRequestDTO {

	private String connectionId;
	private String projectKey;
	private String boardType;
}
