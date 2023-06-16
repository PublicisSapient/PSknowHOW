package com.publicissapient.kpidashboard.common.repository.application;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.KpiCategory;

@Repository
public interface KpiCategoryRepository extends MongoRepository<KpiCategory, ObjectId> {

}
