package com.publicissapient.kpidashboard.common.repository.jira;

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.ReplaceRootOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

@Service
public class SprintRepositoryCustomImpl implements SprintRepositoryCustom {

	@Autowired
	private MongoOperations operations;
	@Autowired
	private SprintRepository sprintRepository;

	private static final String BASIC_PROJECT_CONFIG_ID ="basicProjectConfigId";
	private static final String SPRINTS ="sprints";

	@Override
	public List<SprintDetails> findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(
			Set<ObjectId> basicProjectConfigIds, List<String> sprintStatusList, long limit) {
		MatchOperation matchStage = Aggregation.match(
				Criteria.where(BASIC_PROJECT_CONFIG_ID).in(basicProjectConfigIds).and("state").in(sprintStatusList));

		SortOperation sortStage = Aggregation.sort(Sort.Direction.DESC, "endDate");

		GroupOperation groupStage = Aggregation.group(BASIC_PROJECT_CONFIG_ID).push("$$ROOT").as(SPRINTS);

		ProjectionOperation sliceStage = Aggregation.project().and(SPRINTS).slice((int) limit).as(SPRINTS);

		UnwindOperation unwindStage = Aggregation.unwind(SPRINTS);

		ReplaceRootOperation replaceRootStage = Aggregation.replaceRoot(SPRINTS);
		ProjectionOperation projectStage = Aggregation.project("sprintID", BASIC_PROJECT_CONFIG_ID, "notCompletedIssues",
				"completedIssues", "sprintName", "startDate", "endDate", "completeDate", "totalIssues");

		Aggregation aggregation = Aggregation.newAggregation(matchStage, sortStage, groupStage, sliceStage, unwindStage,
				replaceRootStage, projectStage);

		return operations.aggregate(aggregation, "sprint_details", SprintDetails.class).getMappedResults();
	}

}
