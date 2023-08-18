package com.publicissapient.kpidashboard.common.model.jira;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "happiness_kpi_data")
@EqualsAndHashCode
public class HappinessKpiData extends BasicModel {
    @Indexed
    private ObjectId basicProjectConfigId;
    @Indexed
    private String sprintID;
    private String dateOfSubmission;
    List<UserRatingData> userRatingList;
}
