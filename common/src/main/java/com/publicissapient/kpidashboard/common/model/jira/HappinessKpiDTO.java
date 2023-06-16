package com.publicissapient.kpidashboard.common.model.jira;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HappinessKpiDTO {
	List<UserRatingDTO> userRatingList;
	private String basicProjectConfigId;
	private String sprintID;
	private String dateOfSubmission;
}
