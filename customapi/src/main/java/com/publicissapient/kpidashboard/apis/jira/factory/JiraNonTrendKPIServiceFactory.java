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
package com.publicissapient.kpidashboard.apis.jira.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.NonTrendKPIService;

import lombok.Builder;

/**
 * @author purgupta2
 */
@Service
@Builder
public class JiraNonTrendKPIServiceFactory {
	private static final Map<String, NonTrendKPIService> JIRA_NONTREND_SERVICE_CACHE = new HashMap<>();
	@Autowired
	private List<NonTrendKPIService> services;

	/**
	 * This method return KPI service object on the basis of KPI Id.
	 *
	 * @param type
	 *          KPI id
	 * @return Jira Service object
	 * @throws ApplicationException
	 */
	@SuppressWarnings("rawtypes")
	public static NonTrendKPIService getJiraKPIService(String type) throws ApplicationException {
		NonTrendKPIService service = JIRA_NONTREND_SERVICE_CACHE.get(type);
		if (service == null) {
			throw new ApplicationException(NonTrendKPIService.class, "Jira Non Trend KPI Service Factory not initalized");
		}
		return service;
	}

	/**
	 * This method put all available Jira services to Map where key is the KPI id
	 * and value is the service object.
	 */
	@PostConstruct
	public void initMyServiceCache() {
		for (NonTrendKPIService service : services) {
			JIRA_NONTREND_SERVICE_CACHE.put(service.getQualifierType(), service);
		}
	}
}
