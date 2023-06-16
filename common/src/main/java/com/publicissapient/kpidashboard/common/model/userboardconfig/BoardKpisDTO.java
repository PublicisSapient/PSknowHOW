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
package com.publicissapient.kpidashboard.common.model.userboardconfig;

import com.publicissapient.kpidashboard.common.model.application.KpiMaster;

import lombok.Data;

/**
 * DTO for boardkpis
 * 
 * @author narsingh9
 *
 */
@Data
public class BoardKpisDTO {
	private String kpiId;
	private String kpiName;
	private boolean isEnabled;
	private boolean isShown;
	private int order;
	private String subCategoryBoard;
	private KpiMaster kpiDetail;

	/**
	 * getter for isEnabled
	 * 
	 * @return boolean
	 */
	public boolean getIsEnabled() {
		return this.isEnabled;
	}

	/**
	 * setter for isEnabled
	 * 
	 * @param isEnabled
	 *            isEnabled
	 */
	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
}
