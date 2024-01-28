package com.publicissapient.kpidashboard.apis.model;

import com.publicissapient.kpidashboard.apis.enums.KPICode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataCountKpiData<S> {
    KPICode kpiName;
    List<S> dataCountList;
}
