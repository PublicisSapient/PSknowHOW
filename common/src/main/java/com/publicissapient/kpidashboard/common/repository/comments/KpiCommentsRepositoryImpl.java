package com.publicissapient.kpidashboard.common.repository.comments;

import com.mongodb.client.result.DeleteResult;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

/**
 * @author Mahesh
 *
 */
@Service
public class KpiCommentsRepositoryImpl implements KpiCommentsRepository {

	@Autowired
	private MongoOperations operations;

	@Override
	public List<KPIComments> findCommentsByFilter(String node, String level, String sprintId, String kpiId,
			String sortCommentsResultByOrder) {
		Criteria criteria = new Criteria();
		if (StringUtils.isNotEmpty(kpiId)) {
			criteria = criteria.and("commentsKpiWise.kpiId").is(kpiId);
		}
		if (StringUtils.isNotEmpty(node)) {
			criteria = criteria.and("node").is(node);
		}
		if (StringUtils.isNotEmpty(level)) {
			criteria = criteria.and("level").is(level);
		}
		if (StringUtils.isNotEmpty(sprintId)) {
			criteria = criteria.and("sprintId").is(sprintId);
		}
		Query query = new Query(criteria);

		if (sortCommentsResultByOrder.equalsIgnoreCase("ASC")) {
			query.with(Sort.by(Sort.Direction.ASC, "commentsKpiWise.commentsInfo.commentOn"));
		} else {
			query.with(Sort.by(Sort.Direction.DESC, "commentsKpiWise.commentsInfo.commentOn"));
		}
		return operations.find(query, KPIComments.class);
	}

	@Override
	public void saveIntoCollection(KPIComments kpiComments) {
		operations.save(kpiComments);
	}

	@Override
	public boolean deleteKpiCommentsData(KPIComments oldCommentDocumentId) {

		DeleteResult result = operations.remove(oldCommentDocumentId);
		if (result.getDeletedCount() > 0) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
}
