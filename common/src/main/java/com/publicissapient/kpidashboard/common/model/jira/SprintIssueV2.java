package com.publicissapient.kpidashboard.common.model.jira;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SprintIssueV2 implements Cloneable, Serializable {
	private String key;
	private String originBoardId;
	private String priority;
	private String status;
	private String typeName;
	private String sprintName;
	private String assignee;
	private String description;
	private String updateDate;
	private String summary;
	private Double storyPoints;
	private Double originalEstimate;
	private Double remainingEstimate;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SprintIssueV2 sprintDetails = (SprintIssueV2) o;
		return Objects.equals(key, sprintDetails.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
