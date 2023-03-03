package com.publicissapient.kpidashboard.common.repository.commentshistory;


import com.publicissapient.kpidashboard.common.model.kpicommentshistory.KpiCommentsHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KpiCommentsHistoryRepository extends MongoRepository<KpiCommentsHistory,String> {

}
