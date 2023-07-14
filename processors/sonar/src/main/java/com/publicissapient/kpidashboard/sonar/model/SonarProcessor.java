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

package com.publicissapient.kpidashboard.sonar.model;

import java.util.ArrayList;
import java.util.List;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.generic.Processor;

/**
 * The Sonar Processor which extends processor.
 */
public class SonarProcessor extends Processor {

	private List<String> sonarKpiMetrics = new ArrayList<>();

	/**
	 * Provides instance of Sonar Processor.
	 * 
	 * @param metrics
	 *            the list of metrics
	 * @return SonarProcessor
	 */
	public static SonarProcessor getSonarConfig(List<String> metrics) {
		SonarProcessor sonarInput = new SonarProcessor();
		sonarInput.setProcessorName(ProcessorConstants.SONAR);
		sonarInput.setProcessorType(ProcessorType.SONAR_ANALYSIS);
		sonarInput.setOnline(true);
		sonarInput.setActive(true);
		sonarInput.setLastSuccess(false);
		sonarInput.getSonarKpiMetrics().addAll(metrics);
		return sonarInput;
	}

	/**
	 * Provides Sonar Metrics.
	 * 
	 * @return the list of Sonar KPI Metrics
	 */
	public List<String> getSonarKpiMetrics() {
		return sonarKpiMetrics;
	}

	/**
	 * @param sonarKpiMetrics
	 *            new value of {@link #sonarKpiMetrics}.
	 */
	public void setSonarKpiMetrics(List<String> sonarKpiMetrics) {
		this.sonarKpiMetrics = sonarKpiMetrics;
	}

}
