package com.publicissapient.kpidashboard.common.repository.jira;

import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Component
@Repository
public interface IssueBacklogCustomHistoryRepository extends CrudRepository<IssueBacklogCustomHistory, String>,
		QuerydslPredicateExecutor<IssueBacklogCustomHistory> {

	List<IssueBacklogCustomHistory> findByStoryIDAndBasicProjectConfigId(String storyID, String basicProjectConfigId);

	void deleteByBasicProjectConfigId(String projectID);


}
