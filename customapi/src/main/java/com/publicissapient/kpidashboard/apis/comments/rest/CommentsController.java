package com.publicissapient.kpidashboard.apis.comments.rest;

import com.publicissapient.kpidashboard.apis.comments.service.CommentsService;
import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Objects;


@RestController
@RequestMapping("/comments")
@Slf4j
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * This method will get the comments data based on the selected KPI and selected project/s.
     * @param projectBasicConfig
     * @param kpiId
     * @return
     */
    @GetMapping("/getCommentsByKpiId")
	public ResponseEntity<ServiceResponse> getCommentsByKPI(@RequestParam String projectBasicConfig, String kpiId) {

		final Map<String, Object> mappedCommentInfo = commentsService.findCommentByKPIId(projectBasicConfig, kpiId);
		if (Objects.isNull(mappedCommentInfo) || mappedCommentInfo.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ServiceResponse(true, "Comment not found", mappedCommentInfo));
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(true, "Found comments", mappedCommentInfo));

	}

    /**
     * This method will submit the comment for the selected KPI and selected project.
     * @param comment
     * @return
     */
    @PostMapping("/submitComments")
	public ResponseEntity<ServiceResponse> submitComments(@RequestBody CommentSubmitDTO comment) {

		boolean responseStatus = commentsService.submitComment(comment);
		if (responseStatus) {
			return ResponseEntity.status(HttpStatus.OK).body(
					new ServiceResponse(responseStatus, "Your comment is submitted successfully.", comment));
		} else {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ServiceResponse(responseStatus, "Issue occurred while saving the comment.", comment));
		}
	}
}