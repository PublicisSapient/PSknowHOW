package com.publicissapient.kpidashboard.common.model.jira;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class IssueGroupFields {
	private String storyID;
	private String storyType;
	private String projectComponentId;
	private String subProjectId;
	private String estimate;
	private String createdDate;
	private String priority;
	private String basicProjectConfigId;
	private String url;
}
