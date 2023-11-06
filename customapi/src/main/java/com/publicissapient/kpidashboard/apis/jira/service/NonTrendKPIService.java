/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.jira.service;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;

import java.util.List;
import java.util.Map;

/**
 * @author purgupta2
 */
public interface NonTrendKPIService {

    String getQualifierType();

    /**
     * Gets Kpi data based on kpi request
     *
     * @param kpiRequest
     * @param kpiElement
     * @param filteredNode
     * @return kpi data
     * @throws ApplicationException
     */
    KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
                          Node filteredNode) throws ApplicationException;

    /**
     *
     * @param leafNode
     * @param startDate
     * @param endDate
     * @param kpiRequest
     * @return map of String and Object
     */
    Map<String, Object> fetchKPIDataFromDb(Node leafNode, final String startDate,
                                                  final String endDate, final KpiRequest kpiRequest);
}

