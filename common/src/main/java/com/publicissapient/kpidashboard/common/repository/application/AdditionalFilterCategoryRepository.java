package com.publicissapient.kpidashboard.common.repository.application;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;

@Repository
public interface AdditionalFilterCategoryRepository extends MongoRepository<AdditionalFilterCategory, ObjectId> {
	List<AdditionalFilterCategory> findAllByOrderByLevel();
}
