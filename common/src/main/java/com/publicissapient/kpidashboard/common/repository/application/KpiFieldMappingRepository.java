package com.publicissapient.kpidashboard.common.repository.application;

import com.publicissapient.kpidashboard.common.model.application.KPIFieldMapping;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for {@link KPIFieldMapping}.
 */
public interface KpiFieldMappingRepository extends MongoRepository<KPIFieldMapping, ObjectId> {
}
