package com.publicissapient.kpidashboard.common.repository.comment;

import com.mongodb.client.result.DeleteResult;
import com.publicissapient.kpidashboard.common.model.comment.KPIComments;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class KpiCommentRepositoryImpl implements KpiCommentRepository {
    @Autowired
    private MongoOperations operations;
    @Override
    public List<KPIComments> findByCommentKpiWiseKpiId(String projectBasicConfig, String kpi) {

        Query query = new Query(Criteria.where("projectBasicConfig").is(projectBasicConfig)
                        .and("commentKpiWise.kpiId").is(kpi));

        return operations.find(query, KPIComments.class);

    }

    @Override
    public void saveIntoCollection(KPIComments kpiComments) {
         operations.save(kpiComments);
    }

    @Override
    public boolean deleteKpiCommentsData(KPIComments oldCommentDocumentId) {

        DeleteResult result=operations.remove(oldCommentDocumentId);
        if(result.getDeletedCount() > 0)
        {
          return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public List<KPIComments> findByKpiCommentSortByCommentOn(String projectBasicConfig, String kpi) {
        Query query = new Query(Criteria.where("projectBasicConfig").is(projectBasicConfig)
                .and("commentKpiWise.kpiId").is(kpi));
        query.with(Sort.by(Sort.Direction.ASC,"commentKpiWise.commentInfo.commentOn"));

        return operations.find(query,KPIComments.class);
    }
}
