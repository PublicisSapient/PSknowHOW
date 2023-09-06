package com.publicissapient.kpidashboard.apis.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
public class LeadTimeChangeData {

	private String storyID;

	private String storyType;

	private DateTime createdDate;

	private DateTime closedDate;

	private DateTime releaseDate;

	private double leadTime;

	private String date;
}
