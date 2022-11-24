package com.publicissapient.kpidashboard.common.model.application;

import org.joda.time.DateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Lead time validation data to show in excel.
 */

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LeadTimeValidationDataForKanban {

	private String url;
	private String issueDesc;
	private String issueNumber;
	private DateTime intakeDate;
	private DateTime triageDate;
	private DateTime completedDate;
	private DateTime liveDate;
}
