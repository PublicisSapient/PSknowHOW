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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.filter.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.AccountHierarchyService;

/**
 * @author tauakram
 *
 */
@Service
public class FilterServiceFactory {

	private static final Map<String, AccountHierarchyService<?, ?>> FILTER_SERVICE_CACHE = new HashMap<>();
	@Autowired
	private List<AccountHierarchyService<?, ?>> services;

	/**
	 *
	 * @param type
	 * @return AccountHierarchyService with mathching Qualifier Type
	 * @throws ApplicationException
	 */
	@SuppressWarnings("rawtypes")
	public static AccountHierarchyService getFilterService(String type) throws ApplicationException {
		AccountHierarchyService<?, ?> service = FILTER_SERVICE_CACHE.get(type);
		if (service == null) {
			throw new ApplicationException(FilterServiceFactory.class, "Filter Service Factory not initalized");
		}
		return service;
	}

	/**
	 * Initializes FilterServiceCache with QualifierType as Key
	 */
	@PostConstruct
	public void initMyServiceCache() {
		for (AccountHierarchyService<?, ?> service : services) {
			FILTER_SERVICE_CACHE.put(service.getQualifierType(), service);
		}
	}

}
