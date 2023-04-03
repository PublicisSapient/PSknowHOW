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
        QuerydslPredicateExecutor<IssueBacklogCustomHistory>{

    /**
     * Find by story id list.
     *
     * @param storyID              the story id
     * @param basicProjectConfigId basicProjectConfigId
     * @return the list
     */
    List<IssueBacklogCustomHistory> findByStoryIDAndBasicProjectConfigId(String storyID, String basicProjectConfigId);

    /**
     * Find by story id in list.
     *
     * @param storyList the story list
     * @return the list
     */
    List<IssueBacklogCustomHistory> findByStoryIDIn(List<String> storyList);

    /**
     * Deletes all documents that matches with given projectID.
     *
     * @param projectID String projectID
     */
    void deleteByBasicProjectConfigId(String projectID);

    /**
     * Find by story id list.
     *
     * @param storyID              the story id
     * @param basicProjectConfigId basicProjectConfigId
     * @return the list
     */
    List<IssueBacklogCustomHistory> findByStoryIDInAndBasicProjectConfigIdIn(List<String> storyID,
                                                                          List<String> basicProjectConfigId);

}
