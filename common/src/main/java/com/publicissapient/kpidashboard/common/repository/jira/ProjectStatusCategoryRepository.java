package com.publicissapient.kpidashboard.common.repository.jira;

import com.publicissapient.kpidashboard.common.model.jira.ProjectStatusCategory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectStatusCategoryRepository extends MongoRepository<ProjectStatusCategory, ObjectId> {
    ProjectStatusCategory findByBasicProjectConfigId(String basicProjectConfigId);
}
