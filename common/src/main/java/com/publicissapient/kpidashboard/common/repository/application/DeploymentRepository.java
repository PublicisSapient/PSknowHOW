package com.publicissapient.kpidashboard.common.repository.application;

import java.util.Collection;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.Deployment;

/**
 * Repository for {@link Deployment} data.
 *
 * @author hiren babariya
 */

@Repository
public interface DeploymentRepository extends MongoRepository<Deployment, ObjectId>, DeploymentRepositoryCustom {

	void deleteDeploymentByProjectToolConfigId(Object projectToolConfigId);

	List<Deployment> findByProcessorIdIn(Collection<ObjectId> ids);

	List<Deployment> findByProjectToolConfigIdAndJobName(ObjectId projectToolConfigId, String jobName);

	Deployment findByNumberAndJobNameAndBasicProjectConfigId(String number, String jobName,
			ObjectId basicProjectConfigId);

	Deployment findByProjectToolConfigIdAndNumber(ObjectId projectToolConfigId, String number);

}
