package com.publicissapient.kpidashboard.common.repository.jira;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SprintRepositoryCustom {

	List<SprintDetails> findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(Set<ObjectId> basicProjectConfigIds,
			List<String> sprintStatusList, long limit);

}
