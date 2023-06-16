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

package com.publicissapient.kpidashboard.apis.common.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;

/**
 * REST controller managing all cache request
 *
 * @author anisingh4
 */
@RestController
public class CacheController {

	@Autowired
	private CacheService service;

	/**
	 * Clear Specified cache.
	 *
	 * @param cacheName
	 *            the cache name
	 */
	@RequestMapping(value = "/cache/clearCache/{cacheName}", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public void clearCache(@PathVariable String cacheName) {
		service.clearCache(cacheName);

	}

	/**
	 * Clear all cache.
	 */
	@RequestMapping(value = "/cache/clearAllCache", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public void clearAllCache() {
		service.clearAllCache();
	}

}
