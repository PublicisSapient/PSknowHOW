/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.hierarchy.service;

import java.util.List;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrganizationHierarchyServiceImpl implements OrganizationHierarchyService {

	@Autowired
	private OrganizationHierarchyRepository organizationHierarchyRepository;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public List<OrganizationHierarchy> findAll() {
		return configHelperService.loadAllOrganizationHierarchy();
	}

	/**
	 * all Hierarchy fetching by cache and give particular NodeId
	 *
	 * @param nodeId
	 * @return
	 */
	@Override
	public OrganizationHierarchy findByNodeId(String nodeId) {
		List<OrganizationHierarchy> all = findAll();
		if(CollectionUtils.isNotEmpty(all)){
			return all.stream().filter(node -> node.getNodeId().equals(nodeId)).findFirst().orElse(null);
		}
		return null;
	}

	@Override
	public OrganizationHierarchy save(OrganizationHierarchy organizationHierarchy) {
		return organizationHierarchyRepository.save(organizationHierarchy);
	}

	@Override
	public void deleteByNodeId(String nodeId) {
		organizationHierarchyRepository.deleteByNodeId(nodeId);
	}

	@CacheEvict(CommonConstant.CACHE_ORGANIZATION_HIERARCHY)
	@Override
	public void clearCache() {
		cacheService.clearCache(CommonConstant.CACHE_ACCOUNT_HIERARCHY);
		cacheService.clearCache(CommonConstant.CACHE_ACCOUNT_HIERARCHY_KANBAN);
		cacheService.clearCache(CommonConstant.CACHE_ORGANIZATION_HIERARCHY);
		configHelperService.loadConfigData();
		log.debug("clear cache organization Hierarchies");
	}

	@Override
	public ServiceResponse updateName(String name, String nodeId) {
		OrganizationHierarchy original = findByNodeId(nodeId);
		if (original == null) {
			return new ServiceResponse(false, "No hierarchy node found for nodeId: " + nodeId, null);
		}
		original.setNodeDisplayName(name);
		save(original);
		clearCache();
		return new ServiceResponse(true, "Hierarchy Updated", original);
	}
}
