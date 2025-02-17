package com.publicissapient.kpidashboard.apis.hierarchy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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
		return findAll().stream().filter(node -> node.getNodeId().equals(nodeId)).findFirst().orElse(null);
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
		log.debug("clear cache organization Hierarchies");
	}
}
