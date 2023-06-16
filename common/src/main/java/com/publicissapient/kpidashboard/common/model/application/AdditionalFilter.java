package com.publicissapient.kpidashboard.common.model.application;

import lombok.Data;

import java.util.List;

@Data
public class AdditionalFilter {
    private String filterId;
    private List<AdditionalFilterValue> filterValues;

}
