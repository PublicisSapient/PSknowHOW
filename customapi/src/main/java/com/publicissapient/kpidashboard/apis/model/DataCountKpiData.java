package com.publicissapient.kpidashboard.apis.model;

import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DataCountKpiData {
    KPICode kpiName;
    List<DataCount> dataCountList;
}
