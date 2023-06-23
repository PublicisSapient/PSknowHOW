package com.publicissapient.kpidashboard.apis.debbie.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.publicissapient.kpidashboard.apis.debbie.model.DebbieTools;

public interface DebbieToolsRepository extends MongoRepository<DebbieTools, ObjectId> {

    DebbieTools findByToolName(String toolName);

}
