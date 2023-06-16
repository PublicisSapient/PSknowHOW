package com.publicissapient.kpidashboard.common.model.jira;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IterationStatus {
	private String issueId;
	private String url;
	private String typeName;
	private String issueDescription;
	private String priority;
	private String issueStatus;
	private String dueDate;
	private Integer remainingEstimateMinutes;
	private String delay;
}
