package com.publicissapient.kpidashboard.common.repository.application;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HierarchyLevelRepository extends MongoRepository<HierarchyLevel, ObjectId> {

    List<HierarchyLevel> findAllByOrderByLevel();
}
