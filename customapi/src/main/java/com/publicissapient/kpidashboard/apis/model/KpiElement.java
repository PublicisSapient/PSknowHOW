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

package com.publicissapient.kpidashboard.apis.model;//NOPMD

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumnInfo;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.KpiInfo;
import com.publicissapient.kpidashboard.common.model.application.MaturityLevel;
import com.publicissapient.kpidashboard.common.model.application.TotalDefectAgingResponse;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;

/**
 * Represents Kpi element. KPI detail.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KpiElement implements Serializable { // NOPMD
	// Do not remove NOPMD comment. It ignores TooManyFields violation.
	// This is required for apis to work

	/**
	 *
	 */
	private static final long serialVersionUID = 5188503380533990782L;
	private String kpiId;
	private String kpiName;
	private String fieldName;
	private transient Object value;
	private String unit;
	private transient Object maxValue;
	private String chartType;
	private transient KpiInfo kpiInfo;
	private String id;
	private String isDeleted;
	private String kpiCategory;
	private String kpiInAggregatedFeed;
	private List<String> kpiOnDashboard;
	private String kpiBaseLine;
	private String kpiUnit;
	private Boolean isTrendUpOnValIncrease;
	private Boolean kanban;

	/**
	 * Speedy 2 relevant
	 */
	private String kpiSource;
	private Double thresholdValue;
	private String aggregationType;

	private transient Object trendValueList;
	private transient List<TotalDefectAgingResponse> totalDefectAging;
	private transient Object testCaseExecution;
	private transient Object testExecutionPass;
	private transient List<DataCountGroup> trendValueKpiFilterList;

	private String maturityValue;
	private List<String> maturityRange;

	public List<MaturityLevel> getMaturityLevel() {
		return maturityLevel;
	}

	public void setMaturityLevel(List<MaturityLevel> maturityLevel) {
		this.maturityLevel = maturityLevel;
	}

	private List<MaturityLevel> maturityLevel;

	private List<String> xAxisValues;

	// Excel Data related field. This filed contain vale of a KPI for each node
	// in
	// the tree.
	@JsonIgnore
	private Map<Pair<String, String>, Node> nodeWiseKPIValue;

	// To be used for giving data for the validation functionality on the UI
	@JsonIgnore
	private transient Map<String, ValidationData> mapOfSprintAndData;

	private transient List<DataCount> trendValueListClosedTickets;

	@JsonIgnore
	private transient Map<String, List<DataCount>> trendValueMap;
	private Integer groupId;
	private transient Map<String, String> maturityMap;

	private transient IterationKpiFilters filters;
	private String sprint;
	private List<String> modalHeads;

	@JsonIgnore
	private transient List<KPIExcelData> excelData;
	@JsonIgnore
	private transient List<String> excelColumns;
	// For Excel column Info
	private List<KPIExcelColumnInfo> excelColumnInfo;
	private transient Object filterDuration;
	// used by second screen of DSV for sending all data
	private transient Set<IterationKpiModalValue> issueData;
	// used by first scrren of DSV for filtering
	private transient List<Filter> filterData;
	// used by second screen of DSV for filtering on status
	private transient List<Filter> standUpStatusFilter;
	// used by CycleTime
	private Boolean isAggregationStacks;

	/**
	 * Instantiates a new Kpi element.
	 */
	public KpiElement() {
	}

	/**
	 * Instantiates a new Kpi element.
	 *
	 * @param fieldName
	 *            the field name
	 * @param value
	 *            the value
	 */
	public KpiElement(String fieldName, Object value) {
		this.fieldName = fieldName;
		this.value = value;
	}

	/**
	 * @param kpiElement
	 */
	public KpiElement(KpiElement kpiElement) {
		this.chartType = kpiElement.getChartType();
		this.groupId = kpiElement.getGroupId();
		this.id = kpiElement.getId();
		this.isDeleted = kpiElement.getIsDeleted();
		this.kanban = kpiElement.getKanban();
		this.kpiCategory = kpiElement.getKpiCategory();
		this.kpiId = kpiElement.getKpiId();
		this.kpiName = kpiElement.getKpiName();
		this.kpiSource = kpiElement.getKpiSource();
		this.kpiUnit = kpiElement.getKpiUnit();
		this.maxValue = kpiElement.getMaxValue();
		this.thresholdValue = kpiElement.getThresholdValue();
		this.kpiInfo = kpiElement.getKpiInfo();
		if (null != kpiElement.getValue()) {
			this.value = kpiElement.getValue();
		} else {
			this.value = "undefined";
		}
	}

	public List<String> getExcelColumns() {
		return excelColumns;
	}

	public void setExcelColumns(List<String> excelColumns) {
		this.excelColumns = excelColumns;
	}

	public List<KPIExcelColumnInfo> getExcelColumnInfo() {
		return excelColumnInfo;
	}

	public void setExcelColumnInfo(List<KPIExcelColumnInfo> excelColumnInfo) {
		this.excelColumnInfo = excelColumnInfo;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	/**
	 * Gets map of sprint and data.
	 *
	 * @return the map of sprint and data
	 */
	public Map<String, ValidationData> getMapOfSprintAndData() {
		return mapOfSprintAndData;
	}

	/**
	 * Sets map of sprint and data.
	 *
	 * @param mapOfSprintAndData
	 *            the map of sprint and data
	 */
	public void setMapOfSprintAndData(Map<String, ValidationData> mapOfSprintAndData) {
		this.mapOfSprintAndData = mapOfSprintAndData;
	}

	/**
	 * Gets maturity value.
	 *
	 * @return the maturity value
	 */
	public String getMaturityValue() {
		return maturityValue;
	}

	/**
	 * Sets maturity value.
	 *
	 * @param maturityValue
	 *            the maturity value
	 */
	public void setMaturityValue(String maturityValue) {
		this.maturityValue = maturityValue;
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
	 * Sets kpi id.
	 *
	 * @param kpiId
	 *            the kpi id
	 */
	public void setKpiId(String kpiId) {
		this.kpiId = kpiId;
	}

	/**
	 * Gets kpi name.
	 *
	 * @return the kpi name
	 */
	public String getKpiName() {
		return kpiName;
	}

	/**
	 * Sets kpi name.
	 *
	 * @param kpiName
	 *            the kpi name
	 */
	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}

	/**
	 * Gets value.
	 *
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets value.
	 *
	 * @param value
	 *            the value
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Gets unit.
	 *
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Sets unit.
	 *
	 * @param unit
	 *            the unit
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * Gets max value.
	 *
	 * @return the max value
	 */
	public Object getMaxValue() {
		return maxValue;
	}

	/**
	 * Sets max value.
	 *
	 * @param maxValue
	 *            the max value
	 */
	public void setMaxValue(Object maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * Gets chart type.
	 *
	 * @return the chart type
	 */
	public String getChartType() {
		return chartType;
	}

	/**
	 * Sets chart type.
	 *
	 * @param chartType
	 *            the chart type
	 */
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	/**
	 * Gets field name.
	 *
	 * @return the field name
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Sets field name.
	 *
	 * @param fieldName
	 *            the field name
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * Gets is deleted.
	 *
	 * @return the is deleted
	 */
	public String getIsDeleted() {
		return isDeleted;
	}

	/**
	 * Sets is deleted.
	 *
	 * @param isDeleted
	 *            the is deleted
	 */
	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	/**
	 * Gets kpi category.
	 *
	 * @return the kpi category
	 */
	public String getKpiCategory() {
		return kpiCategory;
	}

	/**
	 * Sets kpi category.
	 *
	 * @param kpiCategory
	 *            the kpi category
	 */
	public void setKpiCategory(String kpiCategory) {
		this.kpiCategory = kpiCategory;
	}

	/**
	 * Gets kpi in aggregated feed.
	 *
	 * @return the kpi in aggregated feed
	 */
	public String getKpiInAggregatedFeed() {
		return kpiInAggregatedFeed;
	}

	/**
	 * Sets kpi in aggregated feed.
	 *
	 * @param kpiInAggregatedFeed
	 *            the kpi in aggregated feed
	 */
	public void setKpiInAggregatedFeed(String kpiInAggregatedFeed) {
		this.kpiInAggregatedFeed = kpiInAggregatedFeed;
	}

	/**
	 * Gets kpi on dashboard.
	 *
	 * @return the kpi on dashboard
	 */
	public List<String> getKpiOnDashboard() {
		return kpiOnDashboard;
	}

	/**
	 * Sets kpi on dashboard.
	 *
	 * @param kpiOnDashboard
	 *            the kpi on dashboard
	 */
	public void setKpiOnDashboard(List<String> kpiOnDashboard) {
		this.kpiOnDashboard = kpiOnDashboard;
	}

	/**
	 * Gets kpi base line.
	 *
	 * @return the kpi base line
	 */
	public String getKpiBaseLine() {
		return kpiBaseLine;
	}

	/**
	 * Sets kpi base line.
	 *
	 * @param kpiBaseLine
	 *            the kpi base line
	 */
	public void setKpiBaseLine(String kpiBaseLine) {
		this.kpiBaseLine = kpiBaseLine;
	}

	/**
	 * Gets kpi unit.
	 *
	 * @return the kpi unit
	 */
	public String getKpiUnit() {
		return kpiUnit;
	}

	/**
	 * Sets kpi unit.
	 *
	 * @param kpiUnit
	 *            the kpi unit
	 */
	public void setKpiUnit(String kpiUnit) {
		this.kpiUnit = kpiUnit;
	}

	/**
	 * Gets is trend up on val increase.
	 *
	 * @return the is trend up on val increase
	 */
	public Boolean getIsTrendUpOnValIncrease() {
		return isTrendUpOnValIncrease;
	}

	/**
	 * Sets is trend up on val increase.
	 *
	 * @param isTrendUpOnValIncrease
	 *            the is trend up on val increase
	 */
	public void setIsTrendUpOnValIncrease(Boolean isTrendUpOnValIncrease) {
		this.isTrendUpOnValIncrease = isTrendUpOnValIncrease;
	}

	/**
	 * Gets kpi source.
	 *
	 * @return the kpi source
	 */
	public String getKpiSource() {
		return kpiSource;
	}

	/**
	 * Sets kpi source.
	 *
	 * @param kpiSource
	 *            the kpi source
	 */
	public void setKpiSource(String kpiSource) {
		this.kpiSource = kpiSource;
	}

	/**
	 * Gets id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets id.
	 *
	 * @param id
	 *            the id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets trend value list.
	 *
	 * @return the trend value list
	 */
	public Object getTrendValueList() {
		return trendValueList;
	}

	/**
	 * Sets trend value list.
	 *
	 * @param trendValueList
	 *            the trend value list
	 */
	public void setTrendValueList(Object trendValueList) {
		this.trendValueList = trendValueList;
	}

	public KpiInfo getKpiInfo() {
		return kpiInfo;
	}

	public void setKpiInfo(KpiInfo kpiInfo) {
		this.kpiInfo = kpiInfo;
	}

	@Override
	public String toString() {
		return "KpiElement [kpiId=" + kpiId + ", kpiName=" + kpiName + ", fieldName=" + fieldName + ", value=" + value
				+ ", unit=" + unit + ", maxValue=" + maxValue + ", chartType=" + chartType + ", id=" + id
				+ ", isDeleted=" + isDeleted + ", kpiCategory=" + kpiCategory + ", kpiInAggregatedFeed="
				+ kpiInAggregatedFeed + ", kpiOnDashboard=" + kpiOnDashboard + ", kpiBaseLine=" + kpiBaseLine
				+ ", kpiUnit=" + kpiUnit + ", isTrendUpOnValIncrease=" + isTrendUpOnValIncrease + ", kpiSource="
				+ kpiSource + ", trendValueList=" + trendValueList + ", filterData=" + filterData;
	}

	/**
	 * Gets threshold value.
	 *
	 * @return the threshold value
	 */
	public Double getThresholdValue() {
		return thresholdValue;
	}

	/**
	 * Sets threshold value.
	 *
	 * @param thresholdValue
	 *            the threshold value
	 */
	public void setThresholdValue(Double thresholdValue) {
		this.thresholdValue = thresholdValue;
	}

	/**
	 * Gets node wise kpi value.
	 *
	 * @return the node wise kpi value
	 */
	public Map<Pair<String, String>, Node> getNodeWiseKPIValue() {
		return nodeWiseKPIValue;
	}

	/**
	 * Sets node wise kpi value.
	 *
	 * @param nodeWiseKPIValue
	 *            the node wise kpi value
	 */
	public void setNodeWiseKPIValue(Map<Pair<String, String>, Node> nodeWiseKPIValue) {
		this.nodeWiseKPIValue = nodeWiseKPIValue;
	}

	/**
	 * Gets maturity range.
	 *
	 * @return the maturity range
	 */
	public List<String> getMaturityRange() {
		return maturityRange;
	}

	/**
	 * Sets maturity range.
	 *
	 * @param maturityRange
	 *            the maturity range
	 */
	public void setMaturityRange(List<String> maturityRange) {
		this.maturityRange = maturityRange;
	}

	public List<String> getxAxisValues() {
		return xAxisValues;
	}

	public void setxAxisValues(List<String> xAxisValues) {
		this.xAxisValues = xAxisValues;
	}

	/**
	 * Gets total defect aging.
	 *
	 * @return the total defect aging
	 */
	public List<TotalDefectAgingResponse> getTotalDefectAging() {
		return totalDefectAging;
	}

	/**
	 * Sets total defect aging.
	 *
	 * @param totalDefectAging
	 *            the total defect aging
	 */
	public void setTotalDefectAging(List<TotalDefectAgingResponse> totalDefectAging) {
		this.totalDefectAging = totalDefectAging;
	}

	/**
	 * Gets test case execution.
	 *
	 * @return the test case execution
	 */
	public Object getTestCaseExecution() {
		return testCaseExecution;
	}

	/**
	 * Sets test case execution.
	 *
	 * @param testCaseExecution
	 *            the test case execution
	 */
	public void setTestCaseExecution(Object testCaseExecution) {
		this.testCaseExecution = testCaseExecution;
	}

	/**
	 * Gets test execution pass.
	 *
	 * @return the test execution pass
	 */
	public Object getTestExecutionPass() {
		return testExecutionPass;
	}

	/**
	 * Sets test execution pass.
	 *
	 * @param testExecutionPass
	 *            the test execution pass
	 */
	public void setTestExecutionPass(Object testExecutionPass) {
		this.testExecutionPass = testExecutionPass;
	}

	/**
	 * Gets kanban.
	 *
	 * @return the kanban
	 */
	public Boolean getKanban() {
		return kanban;
	}

	/**
	 * Sets kanban.
	 *
	 * @param kanban
	 *            the kanban
	 */
	public void setKanban(Boolean kanban) {
		this.kanban = kanban;
	}

	/**
	 * Gets trend value list closed tickets.
	 *
	 * @return the trend value list closed tickets
	 */
	public List<DataCount> getTrendValueListClosedTickets() {
		return trendValueListClosedTickets;
	}

	/**
	 * Sets trend value list closed tickets.
	 *
	 * @param trendValueListClosedTickets
	 *            the trend value list closed tickets
	 */
	public void setTrendValueListClosedTickets(List<DataCount> trendValueListClosedTickets) {
		this.trendValueListClosedTickets = trendValueListClosedTickets;
	}

	/**
	 * Gets aggregation type.
	 *
	 * @return the aggregation type
	 */
	public String getAggregationType() {
		return aggregationType;
	}

	/**
	 * Sets aggregation type.
	 *
	 * @param aggregationType
	 *            the aggregation type
	 */
	public void setAggregationType(String aggregationType) {
		this.aggregationType = aggregationType;
	}

	/**
	 * Gets trend value map.
	 *
	 * @return the trend value map
	 */
	public Map<String, List<DataCount>> getTrendValueMap() {
		return trendValueMap;
	}

	/**
	 * Sets trend value map.
	 *
	 * @param trendValueMap
	 *            the trend value map
	 */
	public void setTrendValueMap(Map<String, List<DataCount>> trendValueMap) {
		this.trendValueMap = trendValueMap;
	}

	/**
	 * @return the trendValueKpiFilterList
	 */
	public List<DataCountGroup> getTrendValueKpiFilterList() {
		return trendValueKpiFilterList;
	}

	/**
	 * @param trendValueKpiFilterList
	 *            the trendValueKpiFilterList to set
	 */
	public void setTrendValueKpiFilterList(List<DataCountGroup> trendValueKpiFilterList) {
		this.trendValueKpiFilterList = trendValueKpiFilterList;
	}

	/**
	 * @return the maturityMap
	 */
	public Map<String, String> getMaturityMap() {
		return maturityMap;
	}

	/**
	 * @param maturityMap
	 *            the maturityMap to set
	 */
	public void setMaturityMap(Map<String, String> maturityMap) {
		this.maturityMap = maturityMap;
	}

	/**
	 * @return the IterationKpiFilters
	 */
	public IterationKpiFilters getFilters() {
		return filters;
	}

	/**
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(IterationKpiFilters filters) {
		this.filters = filters;
	}

	public String getSprint() {
		return sprint;
	}

	public void setSprint(String sprint) {
		this.sprint = sprint;
	}

	public List<String> getModalHeads() {
		return modalHeads;
	}

	public void setModalHeads(List<String> modalHeads) {
		this.modalHeads = modalHeads;
	}

	public List<KPIExcelData> getExcelData() {
		return excelData;
	}

	public void setExcelData(List<KPIExcelData> excelData) {
		this.excelData = excelData;
	}

	public Object getFilterDuration() {
		return filterDuration;
	}

	public void setFilterDuration(Object filterDuration) {
		this.filterDuration = filterDuration;
	}

	public Set<IterationKpiModalValue> getIssueData() {
		return issueData;
	}

	public void setIssueData(Set<IterationKpiModalValue> issueData) {
		this.issueData = issueData;
	}

	public List<Filter> getFilterData() {
		return filterData;
	}

	public void setFilterData(List<Filter> filterData) {
		this.filterData = filterData;
	}

	public List<Filter> getStandUpStatusFilter() {
		return standUpStatusFilter;
	}

	public void setStandUpStatusFilter(List<Filter> standUpStatusFilter) {
		this.standUpStatusFilter = standUpStatusFilter;
	}

	public Boolean getAggregationStacks() {
		return isAggregationStacks;
	}

	public void setAggregationStacks(Boolean aggregationStacks) {
		isAggregationStacks = aggregationStacks;
	}

}
