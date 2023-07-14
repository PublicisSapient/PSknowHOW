package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model class to represent kpi_fieldmapping collection.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "kpi_fieldmapping")
public class KPIFieldMapping extends BasicModel {
	private String kpiId;
	private String kpiName;
	private String kpiSource;
	private List<String> type;
	private Map<String, List<String>> fieldNames;
}
