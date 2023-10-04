package com.publicissapient.kpidashboard.apis.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class LeadTimeChangeData {

	private String storyID;

	private String url;

	private String mergeID;

	private String fromBranch;

	private String closedDate;

	private String releaseDate;

	private double leadTime;

	private String leadTimeInDays;

	private String date;
}
