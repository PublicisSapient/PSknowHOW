package com.publicissapient.kpidashboard.apis.service;

import com.publicissapient.kpidashboard.apis.entity.Resource;

/**
 * @author hargupta15
 */
public interface ResourceService {

	/**
	 * Validate Resource Details
	 * 
	 * @param resourceName
	 *            resourceName
	 * @return Resource
	 */
	Resource validateResource(String resourceName);
}
