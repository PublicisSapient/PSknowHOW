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

public class SymbolValueUnit {
	/**
	 * This is response for jira custom dashboard api service.
	 * 
	 * @author pkum34
	 * 
	 */

	private String symbol;

	private String value;

	private String unit;

	private String trend;

	/**
	 * 
	 * @return symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Sets symbol
	 * 
	 * @param symbol
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * 
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets value
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 
	 * @return unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Sets unit
	 * 
	 * @param unit
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * 
	 * @return trend
	 */
	public String getTrend() {
		return trend;
	}

	/**
	 * Sets trend
	 * 
	 * @param trend
	 */
	public void setTrend(String trend) {
		this.trend = trend;
	}

}
