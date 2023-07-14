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
 * Base class for api request/response having maturity level.
 * 
 * @author anisingh4
 */
public class BaseModel {

	private String maturityLevel;

	/**
	 * Gets maturity level.
	 *
	 * @return the maturity level
	 */
	public String getMaturityLevel() {
		return maturityLevel;
	}

	/**
	 * Sets maturity level.
	 *
	 * @param maturityLevel
	 *            the maturity level
	 */
	public void setMaturityLevel(String maturityLevel) {
		this.maturityLevel = maturityLevel;
	}

}
