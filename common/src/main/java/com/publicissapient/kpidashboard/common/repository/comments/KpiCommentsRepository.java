package com.publicissapient.kpidashboard.common.repository.comments;

import org.springframework.stereotype.Repository;
import  com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import java.util.List;

@Repository
public interface KpiCommentsRepository {

	List<KPIComments> findByCommentKpiWiseKpiId(String projectBasicConfig, String kpiId);

	void saveIntoCollection(KPIComments kpiComments);

	boolean deleteKpiCommentsData(KPIComments oldCommentId);

	List<KPIComments> findByKpiCommentSortByCommentOn(String projectBasicConfig, String kpiId);

}
