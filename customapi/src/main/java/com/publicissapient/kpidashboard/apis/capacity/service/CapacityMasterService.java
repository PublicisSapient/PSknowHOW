package com.publicissapient.kpidashboard.apis.capacity.service;

import com.publicissapient.kpidashboard.common.model.application.CapacityMaster;

import java.util.List;

import org.bson.types.ObjectId;

/**
 * @author narsingh9
 *
 */
public interface CapacityMasterService {

	/**
	 * This method process the capacity data.
	 * @param capacityMaster
	 * @return CapacityMaster object
	 */
	CapacityMaster processCapacityData(CapacityMaster capacityMaster);

	List<CapacityMaster> getCapacities(String basicProjectConfigId);
	
	void deleteCapacityByProject(boolean isKanban, ObjectId basicProjectConfigId);
}
