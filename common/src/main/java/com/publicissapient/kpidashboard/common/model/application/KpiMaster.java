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

package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import com.publicissapient.kpidashboard.common.model.kpivideolink.KPIVideoLink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model class to represent kpi_master collection.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "kpi_master")
public class KpiMaster extends BasicModel {
	private String kpiId;
	private String kpiName;
	private String isDeleted;
	private Integer defaultOrder;
	private String kpiCategory;
	private String kpiSubCategory;
	private String kpiInAggregatedFeed;
	private List<String> kpiOnDashboard;
	private String kpiBaseLine;
	private String kpiUnit;
	private String chartType;
	private String upperThresholdBG;
	private String lowerThresholdBG;
	@JsonProperty("xaxisLabel")
	private String xAxisLabel;
	@JsonProperty("yaxisLabel")
	private String yAxisLabel;
	private boolean showTrend;
	private Boolean isPositiveTrend;
	private String lineLegend;
	private String barLegend;
	private String boxType;
	private boolean calculateMaturity;
	private boolean hideOverallFilter;

	@JsonProperty("videoLink")
	private KPIVideoLink videoLink;

	@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName") // because it will
															  // result in
															  // changing ui and
															  // db
	private Boolean isTrendUpOnValIncrease;

	private String kpiSource;
	private Object maxValue;
	private Double thresholdValue;

	private Boolean kanban;
	private Integer groupId;
	private KpiInfo kpiInfo;
	private String kpiFilter;
	private String aggregationCriteria;
	private String aggregationCircleCriteria;
	private boolean isTrendCalculative;
	private List<KpiFormula> trendCalculation;
	@JsonProperty("isAdditionalFilterSupport")
	private boolean isAdditionalFilterSupport;
	private List<String> maturityRange;
	private Integer kpiWidth;
	private List<MaturityLevel> maturityLevel;
	private Boolean isRepoToolKpi;
	private Map<Integer,String> yaxisOrder;
	private Boolean isAggregationStacks;
}
