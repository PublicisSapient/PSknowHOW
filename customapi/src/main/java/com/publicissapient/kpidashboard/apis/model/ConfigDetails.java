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

package com.publicissapient.kpidashboard.apis.model;

import java.util.Map;

/** Represents Kpi wise aggregation type */
public class ConfigDetails {
	private Map<String, String> kpiWiseAggregationType;
	private Double percentile;
	private int hierarchySelectionCount;
	private DateRangeFilter dateRangeFilter;
	private boolean gitlabToolFieldFlag;
	private int sprintCountForKpiCalculation;
	private boolean isOpenSource;

	public boolean isGitlabToolFieldFlag() {
		return gitlabToolFieldFlag;
	}

	public void setGitlabToolFieldFlag(boolean gitlabToolFieldFlag) {
		this.gitlabToolFieldFlag = gitlabToolFieldFlag;
	}

	public DateRangeFilter getDateRangeFilter() {
		return dateRangeFilter;
	}

	public void setDateRangeFilter(DateRangeFilter dateRangeFilter) {
		this.dateRangeFilter = dateRangeFilter;
	}

	/**
	 * Gets kpi wise aggregation type.
	 *
	 * @return the kpi wise aggregation type
	 */
	public Map<String, String> getKpiWiseAggregationType() {
		return kpiWiseAggregationType;
	}

	/**
	 * Sets kpi wise aggregation type.
	 *
	 * @param kpiWiseAggregationType
	 *          the kpi wise aggregation type
	 */
	public void setKpiWiseAggregationType(Map<String, String> kpiWiseAggregationType) {
		this.kpiWiseAggregationType = kpiWiseAggregationType;
	}

	/**
	 * Gets percentile.
	 *
	 * @return the percentile
	 */
	public Double getPercentile() {
		return percentile;
	}

	/**
	 * Sets percentile.
	 *
	 * @param percentile
	 *          the percentile
	 */
	public void setPercentile(Double percentile) {
		this.percentile = percentile;
	}

	public int getHierarchySelectionCount() {
		return hierarchySelectionCount;
	}

	public void setHierarchySelectionCount(int hierarchySelectionCount) {
		this.hierarchySelectionCount = hierarchySelectionCount;
	}

	public void setNoOfDataPoints(int noOfDataPoints) {
	}

	public void setSprintCountForKpiCalculation(int sprintCountForKpiCalculation) {
		this.sprintCountForKpiCalculation = sprintCountForKpiCalculation;
	}

	public int getSprintCountForKpiCalculation() {
		return sprintCountForKpiCalculation;
	}

    public boolean isOpenSource() {
        return isOpenSource;
    }

    public void setOpenSource(boolean openSource) {
        isOpenSource = openSource;
    }
}
