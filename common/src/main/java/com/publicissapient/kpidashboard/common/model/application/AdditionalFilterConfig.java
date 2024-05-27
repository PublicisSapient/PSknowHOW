package com.publicissapient.kpidashboard.common.model.application;

import java.util.Set;

import lombok.Data;

@Data
public class AdditionalFilterConfig {
	private String filterId;
	private String identifyFrom;
	private String identificationField;
	private Set<String> values;
}
