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

package com.publicissapient.kpidashboard.apis.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.ConfigDetailService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.ConfigDetails;
import com.publicissapient.kpidashboard.apis.model.DateRangeFilter;

/**
 * Implementation of {@link ConfigDetailService}
 * 
 * @author pkum34
 *
 */
@Service
public class ConfigDetailsServiceImpl implements ConfigDetailService {
	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public ConfigDetails getConfigDetails() {
		ConfigDetails configDetails = new ConfigDetails();
		DateRangeFilter dateRangeFilter = new DateRangeFilter(customApiConfig.getDateRangeFilterTypes(),
				customApiConfig.getDateRangeFilterCounts());
		configDetails.setKpiWiseAggregationType(configHelperService.calculateCriteria());
		configDetails.setPercentile(customApiConfig.getPercentileValue());
		configDetails.setHierarchySelectionCount(customApiConfig.getHierarchySelectionCount());
		configDetails.setDateRangeFilter(dateRangeFilter);
		configDetails.setNoOfDataPoints(customApiConfig.getSprintCountForFilters());
		configDetails.setRepoToolFlag(customApiConfig.getIsRepoToolEnable());
		return configDetails;
	}

}
