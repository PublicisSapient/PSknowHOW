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

/**
 * @author tauakram
 *
 */
public class ReleaseFilter implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String startDate;
	private String endDate;

	public ReleaseFilter() {
	}

	/**
	 *
	 * @param sprintId
	 * @param sprintName
	 * @param sprintStartDate
	 * @param sprintEndDate
	 */
	public ReleaseFilter(String sprintId, String sprintName, String sprintStartDate, String sprintEndDate) {
		super();
		this.id = sprintId;
		this.name = sprintName;
		this.startDate = sprintStartDate;
		this.endDate = sprintEndDate;
	}

	/**
	 * 
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * 
	 * @return endDate
	 */
	public String getEndDate() {
		return endDate;
	}

}
