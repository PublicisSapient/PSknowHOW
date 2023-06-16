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

import java.util.List;

/**
 * This is response for jira custom dashboard api service.
 *
 * @author pkum34
 */
public class TrendWidgetsResponse extends BaseResponse {

	private List<String> labels;
	private List<String> data;
	private String startDate;
	private String endDate;
	private String unit;
	private String fromSprint;
	private String toSprint;

	/**
	 * Gets labels.
	 *
	 * @return the labels
	 */
	public List<String> getLabels() {
		return labels;
	}

	/**
	 * Sets labels.
	 *
	 * @param labels
	 *            the labels
	 */
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	/**
	 * Gets data.
	 *
	 * @return the data
	 */
	public List<String> getData() {
		return data;
	}

	/**
	 * Sets data.
	 *
	 * @param data
	 *            the data
	 */
	public void setData(List<String> data) {
		this.data = data;
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
	 * Gets from sprint.
	 *
	 * @return the from sprint
	 */
	public String getFromSprint() {
		return fromSprint;
	}

	/**
	 * Sets from sprint.
	 *
	 * @param fromSprint
	 *            the from sprint
	 */
	public void setFromSprint(String fromSprint) {
		this.fromSprint = fromSprint;
	}

	/**
	 * Gets to sprint.
	 *
	 * @return the to sprint
	 */
	public String getToSprint() {
		return toSprint;
	}

	/**
	 * Sets to sprint.
	 *
	 * @param toSprint
	 *            the to sprint
	 */
	public void setToSprint(String toSprint) {
		this.toSprint = toSprint;
	}

}
