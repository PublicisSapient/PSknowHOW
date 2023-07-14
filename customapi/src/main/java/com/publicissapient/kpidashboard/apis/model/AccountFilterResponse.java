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
 * The Account filter response.
 */
public class AccountFilterResponse extends BaseModel {

	private int level;
	private String label;
	private boolean show;
	private List<AccountFilteredData> filterData;

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
	 * Gets filter data.
	 *
	 * @return the filter data
	 */
	public List<AccountFilteredData> getFilterData() {
		return filterData;
	}

	/**
	 * Sets filter data.
	 *
	 * @param filterData
	 *            the filter data
	 */
	public void setFilterData(List<AccountFilteredData> filterData) {
		this.filterData = filterData;
	}

	/**
	 * Is enabled boolean.
	 *
	 * @return the boolean
	 */
	public boolean isShow() {
		return show;
	}

	/**
	 * Sets enabled.
	 *
	 * @param show
	 *            the enabled
	 */
	public void setShow(boolean show) {
		this.show = show;
	}
}
