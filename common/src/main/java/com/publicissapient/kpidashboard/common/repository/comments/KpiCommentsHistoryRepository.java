package com.publicissapient.kpidashboard.common.repository.comments;


import com.publicissapient.kpidashboard.common.model.kpicommentshistory.KpiCommentsHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


/**
 * @author Mahesh Repository for {@link KpiCommentsHistory}.
 */
@Repository
public interface KpiCommentsHistoryRepository extends MongoRepository<KpiCommentsHistory, String> {


    @Query("{ 'node' : ?0, 'level' : ?1, 'sprintId' : ?2, 'kpiId' : ?3}")
    KpiCommentsHistory findByNodeAndLevelAndSprintIdAndKpiId(String node, String level, String sprintId,String kpiId);

}
