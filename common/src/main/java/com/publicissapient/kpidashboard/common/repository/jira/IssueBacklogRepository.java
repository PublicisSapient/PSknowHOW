package com.publicissapient.kpidashboard.common.repository.jira;

import com.publicissapient.kpidashboard.common.model.jira.IssueBacklog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Component
@Repository
public interface IssueBacklogRepository extends CrudRepository<IssueBacklog, ObjectId>, QuerydslPredicateExecutor<IssueBacklog> {
    /**
     * This essentially returns the max change date from the collection, based
     * on the last change date (or default delta change date property) available
     *
     * @param processorId
     *            Processor ID of source system processor
     * @param changeDate
     *            Last available change date or delta begin date property
     * @return A single Change Date value that is the maximum value of the
     *         existing collection
     */
    @Query
    List<IssueBacklog> findTopByProcessorIdAndChangeDateGreaterThanOrderByChangeDateDesc(ObjectId processorId,
                                                                                      String changeDate);

    /**
     * This essentially returns the max change date from the collection, based
     * on the projectkey and last change date (or default delta change date
     * property) available
     *
     * @param processorId
     *            Processor ID of source system processor
     * @param projectKey
     *            projectKey of the project
     * @param changeDate
     *            Last available change date or delta begin date property
     * @return A single Change Date value that is the maximum value of the
     *         existing collection
     */
    @Deprecated
    @Query
    List<IssueBacklog> findTopByProcessorIdAndProjectKeyAndChangeDateGreaterThanOrderByChangeDateDesc(ObjectId processorId,
                                                                                                   String projectKey, String changeDate);

    /**
     * This essentially returns the max change date from the collection, based
     * on the projectkey and last change date (or default delta change date
     * property) available
     *
     * @param processorId
     *            processor id
     * @param basicProjectConfigId
     *            config project name
     * @param changeDate
     *            change date
     * @return A single Change Date value that is the maximum value of the
     *         existing collection
     */
    @Query
    List<IssueBacklog> findTopByProcessorIdAndBasicProjectConfigIdAndChangeDateGreaterThanOrderByChangeDateDesc(
            ObjectId processorId, String basicProjectConfigId, String changeDate);

    /**
     * This essentially returns the max change date from the collection, based
     * on the basicProjectConfigId(projectConfigId from projectConfig) and last
     * change date
     *
     * @param processorId
     *            processorId
     * @param basicProjectConfigId
     *            projectCOnfigId of project config
     * @param typeName
     *            issue type
     * @param changeDate
     *            change date
     * @return IssueBacklog object
     */
    @Query
    IssueBacklog findTopByProcessorIdAndBasicProjectConfigIdAndTypeNameAndChangeDateGreaterThanOrderByChangeDateDesc(
            ObjectId processorId, String basicProjectConfigId, String typeName, String changeDate);

    /**
     * Gets feature id by id.
     *
     * @param issueId
     *            the s id
     * @param basicProjectConfigId
     *            basicProjectConfigId
     * @return the feature id by id
     */
    @Query(fields = "{'issueId' : 1}")
    List<IssueBacklog> findByIssueIdAndBasicProjectConfigId(String issueId, String basicProjectConfigId);

    /**
     * Gets story by number.
     *
     * @param number
     *            the s number
     * @return the story by number
     */
    @Query(" {'number' : ?0 }")
    List<IssueBacklog> getStoryByNumber(String number);

    List<IssueBacklog> findByNumberAndBasicProjectConfigId(String number, String basicProjectConfigId);

    /**
     * Find one document for given basicProjectConfigId.
     *
     * @param basicProjectConfigId
     *            basicProjectConfigId
     * @return IssueBacklog
     */
    IssueBacklog findTopByBasicProjectConfigId(String basicProjectConfigId);

    /**
     * Deletes all documents that matches with given basicProjectConfigId.
     *
     * @param basicProjectConfigId
     *            basicProjectConfigId
     */
    void deleteByBasicProjectConfigId(String basicProjectConfigId);

    /*
     * Find documents for given numbers and basicProjectConfigId.
     *
     * @param numberIds
     *            List of numbers
     * @param basicProjectConfigId
     *            basicProjectConfigId
     * @return IssueBacklog
     */
    List<IssueBacklog> findByNumberInAndBasicProjectConfigId(List<String> numberIds, String basicProjectConfigId);

    void deleteByNumberAndBasicProjectConfigId(String number, String valueOf);
}