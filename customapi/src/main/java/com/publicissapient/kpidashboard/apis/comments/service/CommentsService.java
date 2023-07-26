package com.publicissapient.kpidashboard.apis.comments.service;

import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentViewResponseDTO;

public interface CommentsService {

	boolean submitComment(CommentSubmitDTO comment);

	Map<String, Object> findCommentByKPIId(String node, String level, String nodeChildId, String kpiId);

	Map<String, Integer> findCommentByBoard(List<String> node, String level, String nodeChildId,
											List<String> kpiId);
	void deleteComments(String commentId);

	List<CommentViewResponseDTO> findLatestCommentSummary(List<String> nodes, String level, String nodeChildId, List<String> kpiIds);

}
