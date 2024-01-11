package com.publicissapient.kpidashboard.common.repository.application;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.publicissapient.kpidashboard.common.model.application.Deployment;

public class DeploymentRepositoryImpl implements DeploymentRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	/**
	 * find deployment list by given projectToolConfigIds , deployment status ,
	 * start and end date wise
	 *
	 * @param mapOfFilters
	 * @param projectBasicConfigIds
	 * @param startDateLD
	 * @param endDateLD
	 * @return
	 */
	@Override
	public List<Deployment> findDeploymentList(Map<String, List<String>> mapOfFilters,
			Set<ObjectId> projectBasicConfigIds, String startDateLD, String endDateLD) {
		Criteria criteria = new Criteria();

		// map of common filters hierarchy and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);

		// date level filters
		criteria = criteria.and("startTime").gte(startDateLD).and("endTime").lte(endDateLD);

		criteria = criteria.and("basicProjectConfigId").in(projectBasicConfigIds);

		Query query = new Query(criteria);

		return mongoTemplate.find(query, Deployment.class);
	}

	/**
	 *
	 * @param mapOfFilters
	 * @param criteria
	 * @return
	 */
	private Criteria getCommonFiltersCriteria(Map<String, List<String>> mapOfFilters, Criteria criteria) {
		Criteria theCriteria = criteria;
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				theCriteria = theCriteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		return theCriteria;
	}

}
