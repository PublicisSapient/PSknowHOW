package com.publicissapient.kpidashboard.apis.comments.service;

import com.publicissapient.kpidashboard.common.model.comment.CommentKpiWise;
import com.publicissapient.kpidashboard.common.model.comment.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comment.KPIComments;

import java.util.List;

public interface CommentService {
   boolean submitComment(CommentSubmitDTO comment);
    List<KPIComments> findCommentByKPIId(String projectBasicConfig,String kpi);

}
