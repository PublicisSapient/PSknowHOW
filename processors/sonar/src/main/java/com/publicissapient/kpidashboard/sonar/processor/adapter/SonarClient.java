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

package com.publicissapient.kpidashboard.sonar.processor.adapter;

import java.util.List;

import org.springframework.http.HttpEntity;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessorItem;

/**
 * Provides features to be developed for Sonar clients.
 *
 */
public interface SonarClient {

	/**
	 * Provides the list of Sonar Projects.
	 * 
	 * @param server
	 *            the Sonar server connection details
	 * @return the list of Sonar project
	 */
	List<SonarProcessorItem> getSonarProjectList(ProcessorToolConnection server);

	/**
	 * Provides latest Sonar Details.
	 * 
	 * @param project
	 *            the Sonar project setup properties
	 * @param httpHeaders
	 *            the list of http header
	 * @param metrics
	 *            the metrics
	 * @return the current sonar details
	 */
	SonarDetails getLatestSonarDetails(SonarProcessorItem project, HttpEntity<String> httpHeaders, String metrics);

	/**
	 * Provides Past Sonar Details.
	 * 
	 * @param project
	 *            the Sonar server connection details
	 * @param httpHeaders
	 *            the list of http header
	 * @param metrics
	 *            the metrics
	 * @return the list of Sonar Data history
	 */
	List<SonarHistory> getPastSonarDetails(SonarProcessorItem project, HttpEntity<String> httpHeaders, String metrics);

}
