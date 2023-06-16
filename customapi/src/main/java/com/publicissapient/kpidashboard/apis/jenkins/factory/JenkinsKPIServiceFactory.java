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

package com.publicissapient.kpidashboard.apis.jenkins.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsKPIService;

/**
 * Factory class for providing service objects.
 */
@Service
public class JenkinsKPIServiceFactory {

	private static final Map<String, JenkinsKPIService<?, ?, ?>> JENKINS_SERVICE_CACHE = new HashMap<>();
	@Autowired
	private List<JenkinsKPIService<?, ?, ?>> services;

	/**
	 * Gets jenkins kpi service.
	 *
	 * @param type
	 *            the type
	 * @return the jenkins kpi service
	 * @throws ApplicationException
	 *             the application exception
	 */
	@SuppressWarnings("rawtypes")
	public static JenkinsKPIService getJenkinsKPIService(String type) throws ApplicationException {
		JenkinsKPIService<?, ?, ?> service = JENKINS_SERVICE_CACHE.get(type);
		if (service == null) {
			throw new ApplicationException(JenkinsKPIServiceFactory.class,
					"Jenkins KPI Service Factory not initalized");
		}
		return service;
	}

	/**
	 * Init service cache.
	 */
	@PostConstruct
	public void initMyServiceCache() {
		for (JenkinsKPIService<?, ?, ?> service : services) {
			JENKINS_SERVICE_CACHE.put(service.getQualifierType(), service);
		}
	}

}
