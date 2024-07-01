package com.publicissapient.kpidashboard.apis.repotools.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class RepoToolUserDetails {
	private String email;
	@JsonProperty("committer__email")
	private String committerEmail;
	private Double average;
	private Long mergeRequests;
	private Long linesChanged;
	private Double hours;
	private Double userReworkRatePercent;
	private Long count;
	@JsonProperty("mr_count")
	private Long mrCount;
	private Map<String, Double> mergeRequestsPT;
}
