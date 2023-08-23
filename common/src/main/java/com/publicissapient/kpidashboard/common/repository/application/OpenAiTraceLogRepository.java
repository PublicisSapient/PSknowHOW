package com.publicissapient.kpidashboard.common.repository.application;

import com.publicissapient.kpidashboard.common.model.application.OpenAiTraceLog;
import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpenAiTraceLogRepository extends MongoRepository<OpenAiTraceLog, ObjectId> {
    OpenAiTraceLog findBySprintId(String sprintId);
}
