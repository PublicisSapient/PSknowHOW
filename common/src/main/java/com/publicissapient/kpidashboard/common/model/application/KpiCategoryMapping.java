package com.publicissapient.kpidashboard.common.model.application;

import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model class to represent kpi_category collection.
 */

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "kpi_category_mapping")
public class KpiCategoryMapping extends BasicModel {

	private String kpiId;
	private String categoryId;
	private Integer kpiOrder;
	private boolean kanban;

}
