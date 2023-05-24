package com.publicissapient.kpidashboard.common.repository.jira;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JiraIssueReleaseStatusRepository extends MongoRepository<JiraIssueReleaseStatus, ObjectId> {
    JiraIssueReleaseStatus findByBasicProjectConfigId(String basicProjectConfigId);
}
