package com.publicissapient.kpidashboard.common.model.jira;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HappinessKpiDTO {
    private String basicProjectConfigId;
    private String sprintID;
    private String dateOfSubmission;
    List<UserRatingDTO> userRatingList;
}
