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

/**
 * Model class to hold start and end date
 *
 * @author anisingh4
 */
public class DateRange {
	private String startDate;
	private String endDate;

	/**
	 * Instantiates a new Date range.
	 */
	public DateRange() {
	}

	/**
	 * Initializes a newly created DateRange with start and end dates
	 *
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 */
	public DateRange(String startDate, String endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
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
}
