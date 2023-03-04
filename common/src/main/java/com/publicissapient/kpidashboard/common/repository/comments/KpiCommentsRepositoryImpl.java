package com.publicissapient.kpidashboard.common.repository.comments;

import com.mongodb.client.result.DeleteResult;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class KpiCommentsRepositoryImpl implements KpiCommentsRepository {

    @Autowired
    private MongoOperations operations;

    @Override
	public List<KPIComments> findByCommentKpiWiseKpiId(String projectBasicConfig, String kpiId) {

		Query query = new Query(
				Criteria.where("projectBasicConfig").is(projectBasicConfig).and("commentsKpiWise.kpiId").is(kpiId));

		return operations.find(query, KPIComments.class);
	}

    @Override
	public void saveIntoCollection(KPIComments kpiComments) {
		operations.save(kpiComments);
	}

    @Override
	public boolean deleteKpiCommentsData(KPIComments oldCommentDocumentId) {

		DeleteResult result = operations.remove(oldCommentDocumentId);
		return result.getDeletedCount() > 0 ? Boolean.TRUE : Boolean.FALSE;
	}

    @Override
    public List<KPIComments> findByKpiCommentSortByCommentOn(String projectBasicConfig, String kpiId) {
        Query query = new Query(Criteria.where("projectBasicConfig").is(projectBasicConfig)
                .and("commentsKpiWise.kpiId").is(kpiId));
        query.with(Sort.by(Sort.Direction.ASC,"commentsKpiWise.commentsInfo.commentOn"));

        return operations.find(query,KPIComments.class);
    }
}
