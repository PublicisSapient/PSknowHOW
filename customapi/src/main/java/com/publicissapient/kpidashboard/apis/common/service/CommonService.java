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

package com.publicissapient.kpidashboard.apis.common.service;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.application.DataCount;

/**
 * The interface Common service to get maturity level.
 * 
 * @author anisingh4
 *
 */
public interface CommonService {

	/**
	 * Gets maturity level.
	 *
	 * @param maturityRangeList
	 *            the maturity range list
	 * @param kpiId
	 *            the kpi type
	 * @param actualMaturityVal
	 *            the actual val
	 * @return the maturity level
	 */
	String getMaturityLevel(List<String> maturityRangeList, String kpiId, String actualMaturityVal);

	/**
	 * 
	 * @param roles
	 *            roles
	 * @return list of email address based on roles
	 */
	public List<String> getEmailAddressBasedOnRoles(List<String> roles);

	/**
	 * This method get list of project admin email address
	 * 
	 * @param projectConfigId
	 *            projectConfigId
	 * @return list of email address based on projectconfigid
	 */
	public List<String> getProjectAdminEmailAddressBasedProjectId(String projectConfigId);

	/**
	 * 
	 * @return String
	 * @throws UnknownHostException
	 */
	public String getApiHost() throws UnknownHostException;

	/**
	 * 
	 * @param trendMap
	 *            trendMap
	 * @return sortedMap
	 */
	public Map<String, List<DataCount>> sortTrendValueMap(Map<String, List<DataCount>> trendMap);

}
