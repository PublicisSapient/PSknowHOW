package com.publicissapient.kpidashboard.common.repository.application;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.publicissapient.kpidashboard.common.model.application.KPIFieldMapping;

/**
 * Repository for {@link KPIFieldMapping}.
 */
public interface KpiFieldMappingRepository extends MongoRepository<KPIFieldMapping, ObjectId> {

    KPIFieldMapping findByKpiId(String kpiId);
}
