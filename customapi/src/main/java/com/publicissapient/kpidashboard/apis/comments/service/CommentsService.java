package com.publicissapient.kpidashboard.apis.comments.service;

import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentViewResponseDTO;

public interface CommentsService {

	boolean submitComment(CommentSubmitDTO comment);

	Map<String, Object> findCommentByKPIId(String node, String level, String sprintId, String kpiId);

	List<CommentViewResponseDTO> findCommentByBoard(String node, String level, String sprintId, List<String> kpiId);
}
