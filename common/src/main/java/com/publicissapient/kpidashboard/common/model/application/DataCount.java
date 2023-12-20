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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data count used for response of all the kpis.
 */
@SuppressWarnings("PMD.TooManyFields")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataCount implements Serializable {
	private static final long serialVersionUID = 1L;

	private String data;
	private Integer count;
	private String priority;
	private String sProjectName;
	private String sSprintID;
	private DateTime deploymentDate;
	private String sStatus;
	private String sSprintName;
	private String sRootCause;
	private transient Object value;
	private transient Object drillDown;
	private String kanbanDate;
	private Map<String, Object> hoverValue;
	private Map<String, ArrayList<Double>> hoverMap;
	private Map<String, Integer> lineHoverValue;
	private String executed;
	private String passed;
	private String subFilter;
	private String lineCategory;
	private String date;
	private Integer noOfRelease;
	private String startDate;
	private String endDate;
	private String kpiGroup;
	private String url;

	private List<String> sprintIds;
	private List<String> sprintNames;
	private List<String> projectNames;
	private String groupBy;
	private String maturity;
	private transient Object maturityValue;
	//dora dashboard
	private transient Object aggregationValue;
	private transient Object lineValue;

	private transient Map<String, Object> subfilterValues;
	private String graphType;// for non-clustered bar and line graph

	private List<DataValue> dataValue; // for multiple line graph
	private transient Object size; // horizontal stacked bar graph

	/**
	 * Instantiates a new Data count.
	 *
	 * @param data
	 *            the data
	 * @param count
	 *            the count
	 */
	public DataCount(String data, Integer count) {
		this.data = data;
		this.count = count;
	}

	/**
	 * Instantiates a new Data count.
	 *
	 * @param data
	 *            the data
	 * @param value
	 *            the value
	 */
	public DataCount(String data, Object value) {
		this.data = data;
		this.value = value;
	}

	/**
	 * Instantiates a drill down data count
	 * 
	 * @param subFilter
	 *            the subFilter
	 * @param value
	 *            the value
	 * @param drillDown
	 *            the drillDown
	 */
	public DataCount(String subFilter, Object value, Object drillDown) {
		this.subFilter = subFilter;
		this.value = value;
		this.drillDown = drillDown;
	}

	/**
	 * Instantiates a drill down data count
	 *
	 * @param subFilter
	 *            the subFilter
	 * @param value
	 *            the value
	 * @param size
	 *            the size
	 * @param drillDown
	 *            the drill down
	 */
	public DataCount(String subFilter, Object value, Object size, Object drillDown) {
		this.subFilter = subFilter;
		this.value = value;
		this.size = size;
		this.drillDown = drillDown;
	}

	/**
	 * Instantiates a new Data count.
	 *
	 * @param data
	 *            the data
	 * @param priority
	 *            the priority
	 * @param value
	 *            the value
	 */
	public DataCount(String data, String priority, Object value) {
		this.data = data;
		this.priority = priority;
		this.value = value;
	}

	/**
	 *
	 * @param data the data
	 * @param maturity maturity
	 * @param maturityValue maturity value
	 * @param value value
	 */
	public DataCount(String data, String maturity, Object maturityValue, Object value) {
		this.data = data;
		this.maturity = maturity;
		this.maturityValue = maturityValue;
		this.value = value;

	}

	/**
	 *
	 * @param data data
	 * @param maturity maturity
	 * @param maturityValue maturity value
	 * @param value value
	 * @param aggregationValue aggregation Value
	 */
	public DataCount(String data, String maturity, Object maturityValue, Object value, Object aggregationValue) {
		this.data = data;
		this.maturity = maturity;
		this.maturityValue = maturityValue;
		this.value = value;
		this.aggregationValue = aggregationValue;
	}

	public String getsSprintID() {
		return sSprintID;
	}

	public String getsSprintName() {
		return sSprintName;
	}
}
