package com.publicissapient.kpidashboard.apis.comments.service;

import com.publicissapient.kpidashboard.common.model.comment.CommentSubmitDTO;

import java.util.Map;

public interface CommentService {
   boolean submitComment(CommentSubmitDTO comment);
    Map<String, Object> findCommentByKPIId(String projectBasicConfig, String kpi);

}
