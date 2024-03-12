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

package com.publicissapient.kpidashboard.apis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.entity.Resource;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.repository.ResourcesRepository;
import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.ResourceService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author hargupta15
 */
@Service
@Slf4j
public class ResourceServiceImpl implements ResourceService {

	@Autowired
	private ResourcesRepository resourcesRepository;

	private MessageService messageService;

	@Autowired
	public ResourceServiceImpl(@Lazy MessageService messageService) {
		this.messageService = messageService;
	}

	/**
	 * Validate Resource Details
	 * 
	 * @param resourceName
	 *            resourceName
	 * @return Resource
	 */
	@Override
	public Resource validateResource(String resourceName) {
		if (resourceName == null || resourceName.isEmpty()) {
			log.error("Invalid resource name : " + resourceName);
			throw new GenericException(messageService.getMessage("error_invalid_resource"));
		}
		Resource resource = resourcesRepository.findByName(resourceName);
		if (resource == null) {
			log.error("Invalid resource name : " + resourceName);
			throw new GenericException(messageService.getMessage("error_invalid_resource"));
		}
		return resource;
	}
}