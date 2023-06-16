package com.publicissapient.kpidashboard.common.model.jira;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "happiness_kpi_data")
@EqualsAndHashCode
public class HappinessKpiData extends BasicModel {
	List<UserRatingData> userRatingList;
	@Indexed
	private ObjectId basicProjectConfigId;
	@Indexed
	private String sprintID;
	private String dateOfSubmission;
}
