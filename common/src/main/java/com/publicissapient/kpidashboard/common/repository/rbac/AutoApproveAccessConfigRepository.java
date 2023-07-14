package com.publicissapient.kpidashboard.common.repository.rbac;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.publicissapient.kpidashboard.common.model.rbac.AutoApproveAccessConfig;

public interface AutoApproveAccessConfigRepository extends MongoRepository<AutoApproveAccessConfig, ObjectId> {
}
