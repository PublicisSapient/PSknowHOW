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

package com.publicissapient.kpidashboard.common.model.sonar;

import com.publicissapient.kpidashboard.common.constant.SonarMetricStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a {@link SonarDetails} metric. Each metric should have a unique
 * name property.
 *
 * @author anisingh4
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SonarMetric {
	private String metricName;
	private Object metricValue;
	private String formattedValue;
	private SonarMetricStatus metricStatus;
	private String message;
	private String date;
	private Object value;

	/**
	 * Instantiates a new Sonar metric.
	 *
	 * @param metricName
	 *            the metricName
	 */
	public SonarMetric(String metricName) {
		this.metricName = metricName;
	}

	/**
	 * Instantiates a new Sonar metric.
	 *
	 * @param metricName
	 *            the metricName
	 * @param metricValue
	 *            the metricValue
	 */
	public SonarMetric(String metricName, Object metricValue) {
		this.metricName = metricName;
		this.metricValue = metricValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		return metricName.equals(((SonarMetric) obj).metricName);
	}

	@Override
	public int hashCode() {
		return metricName.hashCode();
	}

}
