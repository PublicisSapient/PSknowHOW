package com.publicissapient.kpidashboard.common.model.jira;

import lombok.*;
import org.bson.types.ObjectId;

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
