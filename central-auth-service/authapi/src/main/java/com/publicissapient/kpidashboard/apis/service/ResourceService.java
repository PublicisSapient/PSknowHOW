package com.publicissapient.kpidashboard.apis.service;

import com.publicissapient.kpidashboard.apis.entity.Resource;
import com.publicissapient.kpidashboard.apis.errors.GenericException;

public interface ResourceService {

	Resource getResourceByName(String resourceName) throws GenericException;
}
