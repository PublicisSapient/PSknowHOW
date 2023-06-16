package com.publicissapient.kpidashboard.common.repository.jira;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;

@Component
@Repository
public interface IssueBacklogCustomHistoryRepository extends CrudRepository<IssueBacklogCustomHistory, String>,
		QuerydslPredicateExecutor<IssueBacklogCustomHistory> {

	List<IssueBacklogCustomHistory> findByStoryIDAndBasicProjectConfigId(String storyID, String basicProjectConfigId);

	void deleteByBasicProjectConfigId(String projectID);

	List<IssueBacklogCustomHistory> findByStoryIDInAndBasicProjectConfigIdIn(List<String> storyID,
			List<String> basicProjectConfigId);

	List<IssueBacklogCustomHistory> findByStoryIDIn(List<String> storyList);

	@Query(value = "{ 'basicProjectConfigId' : ?0  }", fields = "{ 'storyType' : 1, 'createdDate' : 1,'statusUpdationLog':1}")
	List<IssueBacklogCustomHistory> findByBasicProjectConfigIdIn(String basicProjectConfigId);
}
