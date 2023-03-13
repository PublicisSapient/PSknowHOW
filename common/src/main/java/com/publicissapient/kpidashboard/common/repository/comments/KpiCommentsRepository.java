package com.publicissapient.kpidashboard.common.repository.comments;

import org.springframework.stereotype.Repository;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import java.util.List;

@Repository
public interface KpiCommentsRepository {

	List<KPIComments> findCommentsByFilter(String node, String level, String sprintId, String kpiId,
			String sortCommentsResultByOrder);

	void saveIntoCollection(KPIComments kpiComments);

	boolean deleteKpiCommentsData(KPIComments oldCommentId);

}
