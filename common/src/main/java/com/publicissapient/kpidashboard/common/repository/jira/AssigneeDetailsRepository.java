package com.publicissapient.kpidashboard.common.repository.jira;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;

/**
 * Repository for assignee info of tools like jira , builds , repo.
 */
@Repository
public interface AssigneeDetailsRepository extends MongoRepository<AssigneeDetails, ObjectId> {

	AssigneeDetails findByBasicProjectConfigIdAndSource(String basicProjectConfigId, String source);
}
