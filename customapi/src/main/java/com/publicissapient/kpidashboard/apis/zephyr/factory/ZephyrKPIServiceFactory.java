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
package com.publicissapient.kpidashboard.apis.zephyr.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.zephyr.service.ZephyrKPIService;

/**
 * This class loads all Jira KPIs services and map into Map on the startup of
 * server.
 * 
 * @author tauakram
 */
@Service
public class ZephyrKPIServiceFactory {

	private static final Map<String, ZephyrKPIService<?, ?, ?>> ZEPHYR_SERVICE_CACHE = new HashMap<>();
	@Autowired
	private List<ZephyrKPIService<?, ?, ?>> services;

	/**
	 * This method return KPI service object on the basis of KPI Id.
	 *
	 * @param type
	 *            KPI id
	 * @return Jira Service object
	 * @throws ApplicationException
	 */
	@SuppressWarnings("rawtypes")
	public static ZephyrKPIService getZephyrKPIService(String type) throws ApplicationException {
		ZephyrKPIService<?, ?, ?> service = ZEPHYR_SERVICE_CACHE.get(type);
		if (service == null) {
			throw new ApplicationException(ZephyrKPIServiceFactory.class, "Zephyr KPI Service Factory not initalized");
		}
		return service;
	}

	/**
	 * This method put all available Jira services to Map where key is the KPI id
	 * and value is the service object.
	 */
	@PostConstruct
	public void initMyServiceCache() {
		for (ZephyrKPIService<?, ?, ?> service : services) {
			ZEPHYR_SERVICE_CACHE.put(service.getQualifierType(), service);
		}
	}

}
