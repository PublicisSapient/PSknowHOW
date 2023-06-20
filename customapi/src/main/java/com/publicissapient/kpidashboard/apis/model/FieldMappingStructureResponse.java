package com.publicissapient.kpidashboard.apis.model;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.FieldMappingStructure;
import com.publicissapient.kpidashboard.common.model.application.KPIFieldMapping;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents Kpi fieldmapping response.
 *
 * @author dayshank2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldMappingStructureResponse {
//	private List<KPIFieldMapping> kpiFieldMappingList;
	private List<FieldMappingStructure> fieldMappingStructureList;

}
