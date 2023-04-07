package com.publicissapient.kpidashboard.common.repository.jira;

import com.publicissapient.kpidashboard.common.model.jira.IssueBacklog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueBacklogRepository
		extends CrudRepository<IssueBacklog, ObjectId>, QuerydslPredicateExecutor<IssueBacklog>, IssueBacklogRespositoryCustom {

	@Query(fields = "{'issueId' : 1}")
	List<IssueBacklog> findByIssueIdAndBasicProjectConfigId(String issueId, String basicProjectConfigId);

	void deleteByBasicProjectConfigId(String basicProjectConfigId);

}