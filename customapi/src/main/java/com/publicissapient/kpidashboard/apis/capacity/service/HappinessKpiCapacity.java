package com.publicissapient.kpidashboard.apis.capacity.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiDTO;

public interface HappinessKpiCapacity {
    /**
     * This method process the capacity data.
     * @param happinessKpiDTO
     * @return ServiceResponse object
     */
    public ServiceResponse saveHappinessKpiData(HappinessKpiDTO happinessKpiDTO);
}
