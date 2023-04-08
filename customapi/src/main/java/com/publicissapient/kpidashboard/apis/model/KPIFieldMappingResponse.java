package com.publicissapient.kpidashboard.apis.model;

import com.publicissapient.kpidashboard.common.model.application.KPIFieldMapping;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents Kpi fieldmapping response.
 *
 * @author dayshank2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KPIFieldMappingResponse {
    private List<KPIFieldMapping> kpiFieldMappingList;
}
