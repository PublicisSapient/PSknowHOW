package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Lead time data to show in excel.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LeadTimeData {

	private List<String> issueNumber;
	private List<String> urlList;
	private List<String> issueDiscList;

	private List<String> intakeToDor;
	private List<String> dorToDOD;
	private List<String> dodToLive;
	private List<String> intakeToLive;

	private List<String> openToTriage;
	private List<String> triageToComplete;
	private List<String> completeToLive;
	private List<String> leadTime;

}
