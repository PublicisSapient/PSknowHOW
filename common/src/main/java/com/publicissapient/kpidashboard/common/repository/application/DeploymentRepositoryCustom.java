package com.publicissapient.kpidashboard.common.repository.application;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.Deployment;

/**
 * The interface {@link Deployment} repository custom.
 *
 * @author hiren babariya
 */
public interface DeploymentRepositoryCustom {

	List<Deployment> findDeploymentList(Map<String, List<String>> mapOfFilters, Set<ObjectId> projectBasicConfigIds,
			String startDateUTC, String endDateUTC);

}
