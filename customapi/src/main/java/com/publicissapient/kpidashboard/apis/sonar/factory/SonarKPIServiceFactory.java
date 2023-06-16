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

package com.publicissapient.kpidashboard.apis.sonar.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarKPIService;

@Service
public class SonarKPIServiceFactory {

	private static final Map<String, SonarKPIService<?, ?, ?>> SONAR_SERVICE_CACHE = new HashMap<>();
	@Autowired
	private List<SonarKPIService<?, ?, ?>> services;

	/**
	 * Gets sonar KPI service
	 *
	 * @param type
	 * @return SonarKPIService
	 * @throws ApplicationException
	 */
	@SuppressWarnings("rawtypes")
	public static SonarKPIService getSonarKPIService(String type) throws ApplicationException {
		SonarKPIService<?, ?, ?> service = SONAR_SERVICE_CACHE.get(type);
		if (service == null) {
			throw new ApplicationException(SonarKPIServiceFactory.class, "Sonar KPI Service Factory not initalized");
		}
		return service;
	}

	/**
	 * Initializes SonarKPIService
	 */
	@PostConstruct
	public void initMyServiceCache() {
		for (SonarKPIService<?, ?, ?> service : services) {
			SONAR_SERVICE_CACHE.put(service.getQualifierType(), service);
		}
	}

}
