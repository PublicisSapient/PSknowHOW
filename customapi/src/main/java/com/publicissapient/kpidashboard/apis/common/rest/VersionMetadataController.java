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
package com.publicissapient.kpidashboard.apis.common.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.VersionMetadataService;
import com.publicissapient.kpidashboard.apis.model.VersionDetails;

/**
 * Rest controller to handle version related requests.
 *
 * @author vijmishr1
 */
@RestController
public class VersionMetadataController {

	private static final Logger LOGGER = LoggerFactory.getLogger(VersionMetadataController.class);

	@Autowired
	private VersionMetadataService versionMetadataService;

	/**
	 * Gets version details.
	 *
	 * @return the version details
	 */
	@RequestMapping(value = "/getversionmetadata", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public VersionDetails getVersionDetails() {

		LOGGER.debug("VersionMetadataController::getVersionDetails start");
		return versionMetadataService.getVersionMetadata();
	}

}
