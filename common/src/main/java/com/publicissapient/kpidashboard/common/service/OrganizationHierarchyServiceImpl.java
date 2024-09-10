package com.publicissapient.kpidashboard.common.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrganizationHierarchyServiceImpl implements OrganizationHierarchyService {

	@Autowired
	private OrganizationHierarchyRepository organizationHierarchyRepository;

	@Cacheable(CommonConstant.CACHE_ORGANIZATION_HIERARCHY)
	@Override
	public List<OrganizationHierarchy> findAll() {
		log.debug("created cache organization Hierarchies");
		return organizationHierarchyRepository.findAll();
	}

	@Override
	public OrganizationHierarchy findById(String nodeId) {
		return organizationHierarchyRepository.findByNodeId(nodeId);
	}

	@CacheEvict(CommonConstant.CACHE_ORGANIZATION_HIERARCHY)
	@Override
	public void clearCache() {
		log.debug("clear cache organization Hierarchies");
	}
}
