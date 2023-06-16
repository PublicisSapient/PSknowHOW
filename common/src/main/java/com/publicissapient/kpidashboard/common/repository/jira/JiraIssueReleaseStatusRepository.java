package com.publicissapient.kpidashboard.common.repository.jira;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;

public interface JiraIssueReleaseStatusRepository extends MongoRepository<JiraIssueReleaseStatus, ObjectId> {
	JiraIssueReleaseStatus findByBasicProjectConfigId(String basicProjectConfigId);
}
