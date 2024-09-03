package com.publicissapient.kpidashboard.common.service;

import com.publicissapient.kpidashboard.common.model.application.HierarchyMaster;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HierarchyMasterServiceImpl implements HierarchyMasterService {

	@Autowired
	private HierarchyMasterRepository hierarchyMasterRepository;

	@Override public List<HierarchyMaster> findAllHierarchiesList() {
		return hierarchyMasterRepository.findAll();
	}
}
