package com.publicissapient.kpidashboard.common.repository.application;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationHierarchyRepository extends MongoRepository<OrganizationHierarchy, ObjectId> {

	OrganizationHierarchy findByNodeId(String nodeId);

}
