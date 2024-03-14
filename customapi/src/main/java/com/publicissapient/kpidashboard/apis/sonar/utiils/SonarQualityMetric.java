/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.sonar.utiils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Used to store the data for sonar code quality metric calculation
 * 
 * @author shunaray
 * @link <a href=
 *       "https://publicissapient.atlassian.net/wiki/spaces/SPDS/pages/79822877/KnowHOW+TEngine+KPIs+on+KnowHOW">Refer
 *       Docs.</a>
 */
@Data
public class SonarQualityMetric {
	private List<Long> numeratorLeft;
	private List<Long> denominatorLeft;
	private List<Long> numeratorRight;
	private List<Long> denominatorRight;
	private long costPerLine;

	public SonarQualityMetric() {
		this.numeratorLeft = new ArrayList<>();
		this.denominatorLeft = new ArrayList<>();
		this.numeratorRight = new ArrayList<>();
		this.denominatorRight = new ArrayList<>();
		this.costPerLine = 0L;
	}

	/**
	 * Method for adding data related to quality metrics calculation
	 * 
	 * @param numeratorLeft
	 *            numeratorLeft
	 * @param denominatorLeft
	 *            denominatorLeft
	 * @param numeratorRight
	 *            numeratorRight
	 * @param denominatorRight
	 *            denominatorRight
	 * @param costPerLine
	 *            costPerLine
	 */
	public void addQualityMetrics(long numeratorLeft, long denominatorLeft, long numeratorRight, long denominatorRight,
			long costPerLine) {
		this.numeratorLeft.add(numeratorLeft);
		this.denominatorLeft.add(denominatorLeft);
		this.numeratorRight.add(numeratorRight);
		this.denominatorRight.add(denominatorRight);
		this.costPerLine = costPerLine;
	}

	/**
	 * Used to calculate RemediationEffortChange
	 *
	 * @return RemediationEffortChange
	 * @link <a href=
	 *       "https://publicissapient.atlassian.net/wiki/spaces/SPDS/pages/79822877/KnowHOW+TEngine+KPIs+on+KnowHOW">Refer
	 *       Docs.</a>
	 */
	public double getTEngineData() {
		double ratioRight = calculateRatio(this.numeratorRight, this.denominatorRight, this.costPerLine);
		double ratioLeft = calculateRatio(this.numeratorLeft, this.denominatorLeft, this.costPerLine);
		return ((ratioLeft != 0) ? (ratioRight / ratioLeft) * 100 : 0);
	}

	/**
	 * Used to calculate Security , Maintainability, Duplicate & Reliability Ratio %
	 *
	 * @param numerator
	 *            numerator
	 * @param denominator
	 *            denominator
	 * @param costPerLine
	 *            costPerLine
	 * @return double
	 * @link <a href=
	 *       "https://publicissapient.atlassian.net/wiki/spaces/SPDS/pages/79822877/KnowHOW+TEngine+KPIs+on+KnowHOW">Refer
	 *       Docs.</a>
	 */
	public double calculateRatio(List<Long> numerator, List<Long> denominator, long costPerLine) {
		long sumNumerator = numerator.stream().mapToLong(Long::longValue).sum();
		long sumDenominator = denominator.stream().mapToLong(Long::longValue).sum();
		return ((sumDenominator != 0) ? ((double) sumNumerator / sumDenominator) * costPerLine : 0);
	}

}
