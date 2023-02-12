package com.publicissapient.kpidashboard.apis.pushdata.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;

@Repository
public interface ExposeApiTokenRepository extends MongoRepository<ExposeApiToken, ObjectId> {

	ExposeApiToken findByUserNameAndBasicProjectConfigId(String userName , ObjectId basicProjectConfigId);

	ExposeApiToken findByApiToken(String apiToken);

}
