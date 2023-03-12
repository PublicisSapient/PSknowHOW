/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.enums;

import java.util.Arrays;
import java.util.List;

/**
 * For Excel Column Info
 */
public enum KPIExcelColumnInfo {
    OVERALL_COMPLETION_STATUS("kpi128",
            Arrays.asList("","","","","","","","","",
                    "Delay is calculated based on difference between time taken to complete an issue that depends on the Actual Start date and Actual completion date (In Days) and the Original Estimate (In Days)"));

    private String kpiId;

    private List<String> columnsInfo;

    KPIExcelColumnInfo(String kpiID, List<String> columnsInfo) {
        this.kpiId = kpiID;
        this.setColumnsInfo(columnsInfo);
    }

    /**
     * Gets kpi id.
     *
     * @return the kpi id
     */
    public String getKpiId() {
        return kpiId;
    }


    /**
     * Gets source.
     *
     * @return the source
     */
    public List<String> getColumnsInfo() {
        return columnsInfo;
    }

    /**
     * Sets source.
     *
     * @return the source
     */
    private void setColumnsInfo(List<String> columnsInfo) {
        this.columnsInfo = columnsInfo;
    }
}
