package com.publicissapient.kpidashboard.apis.capacity.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.CapacityMaster;

/**
 * @author narsingh9
 *
 */
public interface CapacityMasterService {

	/**
	 * This method process the capacity data.
	 * 
	 * @param capacityMaster
	 * @return CapacityMaster object
	 */
	CapacityMaster processCapacityData(CapacityMaster capacityMaster);

	List<CapacityMaster> getCapacities(String basicProjectConfigId);

	JsonNode getRepoToolAssigneeEmail(String projectConfigId);

	void deleteCapacityByProject(boolean isKanban, ObjectId basicProjectConfigId);
}
