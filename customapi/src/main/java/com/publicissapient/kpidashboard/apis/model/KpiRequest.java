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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

/**
 * The type Kpi request.
 */
public class KpiRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String requestTrackerId;
	private int level;
	private String label;
	private String[] ids;
	private String[] platformIds;
	private List<KpiElement> kpiList;
	private Map<String, List<String>> selectedMap;
	private String startDate;
	private String endDate;
	private String filterToShowOnTrend = Constant.PROJECT;
	private List<String> sprintIncluded;
	private String selecedHierarchyLabel;
	private int kanbanXaxisDataPoints = 7;
	private int xAxisDataPoints = 5;
	private String duration = CommonConstant.MONTH;

	/**
	 * Gets kpi list.
	 *
	 * @return the kpi list
	 */
	public List<KpiElement> getKpiList() {
		return kpiList;
	}

	/**
	 * Sets kpi list.
	 *
	 * @param kpiList
	 *            the kpi list
	 */
	public void setKpiList(List<KpiElement> kpiList) {
		this.kpiList = kpiList;
		setRequestTrackerId();
	}

	/**
	 * Gets level.
	 *
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Sets level.
	 *
	 * @param level
	 *            the level
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Get ids string [ ].
	 *
	 * @return the string [ ]
	 */
	public String[] getIds() {
		return ids == null ? null : ids.clone();
	}

	/**
	 * Sets ids.
	 *
	 * @param ids
	 *            the ids
	 */
	public void setIds(String[] ids) {
		this.ids = ids == null ? null : ids.clone();
	}

	/**
	 * Get platform ids string [ ].
	 *
	 * @return the string [ ]
	 */
	public String[] getPlatformIds() {
		return platformIds == null ? null : platformIds.clone();
	}

	/**
	 * Sets platform ids.
	 *
	 * @param platformIds
	 *            the platform ids
	 */
	public void setPlatformIds(String[] platformIds) {
		this.platformIds = platformIds == null ? null : platformIds.clone();
	}

	/**
	 * Gets request tracker id.
	 *
	 * @return the request tracker id
	 */
	public String getRequestTrackerId() {
		return requestTrackerId;
	}

	/**
	 * Sets request tracker id.
	 */
	public void setRequestTrackerId() {
		String kpiSource = "";
		if (null != getKpiList() && !getKpiList().isEmpty()) {
			kpiSource = getKpiList().get(0).getKpiSource();
		}
		this.requestTrackerId = kpiSource + "-" + UUID.randomUUID().toString();
	}

	/**
	 * Gets label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets label.
	 *
	 * @param label
	 *            the label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets selected map.
	 *
	 * @return the selected map
	 */
	public Map<String, List<String>> getSelectedMap() {
		return selectedMap;
	}

	/**
	 * Sets selected map.
	 *
	 * @param selectedMap
	 *            the selected map
	 */
	public void setSelectedMap(Map<String, List<String>> selectedMap) {
		this.selectedMap = selectedMap;
	}

	/**
	 * Gets start date.
	 *
	 * @return the start date
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * Sets start date.
	 *
	 * @param startDate
	 *            the start date
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * Gets end date.
	 *
	 * @return the end date
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * Sets end date.
	 *
	 * @param endDate
	 *            the end date
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * Gets filter to show on trend.
	 *
	 * @return the filter to show on trend
	 */
	public String getFilterToShowOnTrend() {
		return filterToShowOnTrend;
	}

	/**
	 * Sets filter to show on trend.
	 *
	 * @param filterToShowOnTrend
	 *            the filter to show on trend
	 */
	public void setFilterToShowOnTrend(String filterToShowOnTrend) {
		this.filterToShowOnTrend = filterToShowOnTrend;
	}

	/**
	 *
	 * @return selecedHierarchyLabel
	 */
	public String getSelecedHierarchyLabel() {
		return selecedHierarchyLabel;
	}

	/**
	 *
	 * @param selecedHierarchyLabel
	 *            selecedHierarchyLabel
	 */
	public void setSelecedHierarchyLabel(String selecedHierarchyLabel) {
		this.selecedHierarchyLabel = selecedHierarchyLabel;
	}

	/**
	 * @return sprintIncluded
	 */
	public List<String> getSprintIncluded() {
		return this.sprintIncluded;
	}

	/**
	 * @param sprintIncluded
	 *            sprintIncluded
	 */
	public void setSprintIncluded(List<String> sprintIncluded) {
		this.sprintIncluded = sprintIncluded;
	}

	public int getKanbanXaxisDataPoints() {
		return kanbanXaxisDataPoints;
	}

	public void setKanbanXaxisDataPoints(int kanbanXaxisDataPoints) {
		this.kanbanXaxisDataPoints = kanbanXaxisDataPoints;
	}
	
	public int getXAxisDataPoints() {
		return xAxisDataPoints;
	}

	public void setXAxisDataPoints(int xAxisDataPoints) {
		this.xAxisDataPoints = xAxisDataPoints;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "KpiRequest [requestTrackerId=" + requestTrackerId + ", level=" + level + ", ids=" + Arrays.toString(ids)
				+ ", platformIds=" + Arrays.toString(platformIds) + ", kpiList=" + kpiList + "]" + "sprintIncluded "
				+ sprintIncluded;
	}

}
