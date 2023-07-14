package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;

import lombok.Data;

@Data
public class AdditionalFilter {
	private String filterId;
	private List<AdditionalFilterValue> filterValues;

}
