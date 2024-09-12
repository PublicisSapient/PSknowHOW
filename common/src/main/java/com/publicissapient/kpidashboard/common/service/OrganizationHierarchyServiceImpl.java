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

	/**
	 * all Hierarchy fetching by cache and give particular NodeId
	 * @param nodeId
	 * @return
	 */
	@Override
	public OrganizationHierarchy findByNodeId(String nodeId) {
		return findAll().stream().filter(node -> node.getNodeId().equals(nodeId)).findFirst().orElse(null);
	}

	@Override
	public OrganizationHierarchy save(OrganizationHierarchy organizationHierarchy){
		return organizationHierarchyRepository.save(organizationHierarchy);
	}

	@CacheEvict(CommonConstant.CACHE_ORGANIZATION_HIERARCHY)
	@Override
	public void clearCache() {
		log.debug("clear cache organization Hierarchies");
	}
}
