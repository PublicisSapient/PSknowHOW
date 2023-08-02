package com.publicissapient.kpidashboard.apis.repotools.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsProvider;

public interface RepoToolsProviderRepository extends MongoRepository<RepoToolsProvider, ObjectId> {

    RepoToolsProvider findByToolName(String toolName);

}
