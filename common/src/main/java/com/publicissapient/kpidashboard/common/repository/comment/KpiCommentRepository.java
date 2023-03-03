package com.publicissapient.kpidashboard.common.repository.comment;

import org.springframework.stereotype.Repository;
import  com.publicissapient.kpidashboard.common.model.comment.KPIComments;
import java.util.List;

@Repository
public interface KpiCommentRepository  {

  List<KPIComments>  findByCommentKpiWiseKpiId(String projectBasicConfig, String kpi);

  void saveIntoCollection(KPIComments kpiComments);


  public boolean deleteKpiCommentsData(KPIComments oldCommentId);




    List<KPIComments> findByKpiCommentSortByCommentOn(String projectBasicConfig, String kpi);

}
