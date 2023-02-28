package com.publicissapient.kpidashboard.common.repository.comment;

import com.publicissapient.kpidashboard.common.model.comment.CommentKpiWise;
import com.publicissapient.kpidashboard.common.model.comment.KPIComments;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KpiCommentRepository extends CrudRepository<KPIComments, String> {
   @Query("{  'projectBasicConfig' : ?0,'commentKpiWise.KpiId' : ?1}")
   List<KPIComments> findByCommentKpiWiseKpiId(String projectBasicConfig,String kpi);
}
