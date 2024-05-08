package com.publicissapient.kpidashboard.apis.service.impl;

import com.publicissapient.kpidashboard.apis.entity.Resource;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.repository.ResourcesRepository;
import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.ResourceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author hargupta15
 */
@Service
@AllArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {
    private final ResourcesRepository resourcesRepository;

    private final MessageService messageService;

    /**
     * Validate Resource Details
     *
     * @param resourceName resourceName
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