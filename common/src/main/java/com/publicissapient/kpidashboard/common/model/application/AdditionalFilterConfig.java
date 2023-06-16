package com.publicissapient.kpidashboard.common.model.application;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class AdditionalFilterConfig {
    private String filterId;
    private String identifyFrom;
    private String identificationField;
    private Set<String> values;
}
