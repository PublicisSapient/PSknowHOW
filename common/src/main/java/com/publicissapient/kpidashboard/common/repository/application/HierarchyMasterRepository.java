package com.publicissapient.kpidashboard.common.repository.application;

import com.publicissapient.kpidashboard.common.model.application.HierarchyMaster;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HierarchyMasterRepository extends MongoRepository<HierarchyMaster, ObjectId> {

	HierarchyMaster findByNodeId(String nodeId);

}
