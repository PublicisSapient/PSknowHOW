package com.publicissapient.kpidashboard.apis.jira.model;

import java.util.List;

import lombok.Data;

/**
 * Search in board list response data.
 *
 */
@Data
public class JiraBoardListResponse {

	private long startAt;
	private int maxResults;
	private long total;
	private String isLast;
	private List<JiraBoardValueResponse> values;
}
