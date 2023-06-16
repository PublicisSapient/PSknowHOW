package com.publicissapient.kpidashboard.common.repository.application;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdditionalFilterCategoryRepository extends MongoRepository<AdditionalFilterCategory, ObjectId> {
    List<AdditionalFilterCategory> findAllByOrderByLevel();
}
