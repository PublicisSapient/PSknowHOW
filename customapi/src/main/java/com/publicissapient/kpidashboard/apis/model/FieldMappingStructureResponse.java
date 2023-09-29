package com.publicissapient.kpidashboard.apis.model;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.FieldMappingStructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldMappingStructureResponse {
	private List<FieldMappingStructure> fieldConfiguration;
	private String kpiSource;
	private String projectToolConfigId;

}
