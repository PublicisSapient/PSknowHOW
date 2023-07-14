package com.publicissapient.kpidashboard.common.repository.application;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevelSuggestion;

/**
 * interface for hierarchy levels values Repository
 *
 * @author Hiren Babariya
 *
 */
@Repository
public interface HierarchyLevelSuggestionRepository extends MongoRepository<HierarchyLevelSuggestion, ObjectId> {

	HierarchyLevelSuggestion findByHierarchyLevelId(String hierarchyLevelId);

}
