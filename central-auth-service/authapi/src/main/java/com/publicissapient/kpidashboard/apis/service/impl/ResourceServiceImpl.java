package com.publicissapient.kpidashboard.apis.service.impl;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.entity.Resource;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.repository.ResourcesRepository;
import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.ResourceService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {
	private final ResourcesRepository resourcesRepository;

	private final MessageService messageService;

	@Override
	public Resource getResourceByName(String resourceName) throws GenericException {
		return resourcesRepository.findByName(resourceName)
				.orElseThrow(() -> new GenericException(messageService.getMessage("error_invalid_resource")));
	}
}
