package com.publicissapient.kpidashboard.apis.comments.service;

import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;

import java.util.Map;

public interface CommentsService {

	boolean submitComment(CommentSubmitDTO comment);

	Map<String, Object> findCommentByKPIId(String node, String level, String sprintId, String kpiId);
}
