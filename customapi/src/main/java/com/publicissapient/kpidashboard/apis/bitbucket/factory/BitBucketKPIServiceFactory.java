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

package com.publicissapient.kpidashboard.apis.bitbucket.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.bitbucket.service.BitBucketKPIService;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;

/**
 * Factory to provide bit bucket service.
 *
 * @author anisingh4
 */
@Service
public class BitBucketKPIServiceFactory {

	private static final Map<String, BitBucketKPIService<?, ?, ?>> BIT_BUCKET_SERVICE_CACHE = new HashMap<>();
	@Autowired
	private List<BitBucketKPIService<?, ?, ?>> services;

	@SuppressWarnings("rawtypes")
	public static BitBucketKPIService getBitBucketKPIService(String type) throws ApplicationException {
		BitBucketKPIService<?, ?, ?> service = BIT_BUCKET_SERVICE_CACHE.get(type);
		if (service == null) {
			throw new ApplicationException(BitBucketKPIServiceFactory.class,
					"Bitbucket KPI Service Factory not initalized");
		}
		return service;
	}

	@PostConstruct
	public void initMyServiceCache() {
		for (BitBucketKPIService<?, ?, ?> service : services) {
			BIT_BUCKET_SERVICE_CACHE.put(service.getQualifierType(), service);
		}
	}

}