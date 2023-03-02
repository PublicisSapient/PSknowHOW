package com.publicissapient.kpidashboard.common.repository.comment;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import  com.publicissapient.kpidashboard.common.model.comment.KPIComments;
import java.util.List;

@Repository
public interface KpiCommentRepository extends MongoRepository<KPIComments, String> {
  @Query("{  'projectBasicConfig' : ?0,'commentKpiWise.KpiId' : ?1}")
  List<KPIComments>  findByCommentKpiWiseKpiId(String projectBasicConfig, String kpi);

    @Query("{  'projectBasicConfig' : ?0,'commentKpiWise.KpiId' : ?1}")
    List<KPIComments> findByCommentKpiWiseKpiIdList(String projectBasicConfig, String kpi);
}
