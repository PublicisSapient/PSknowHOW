package com.publicissapient.kpidashboard.apis.comments.rest;

import com.publicissapient.kpidashboard.common.model.comment.CommentSubmitDTO;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.comments.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/comments")
@Slf4j
public class CommentController {

    @Autowired
    private CommentService commentService;

@GetMapping("/getCommentsByKpiId")
public ResponseEntity<ServiceResponse> getCommentsByKPI(@RequestParam String projectBasicConfig, String kpi) {

    final Map<String, Object> mappedCommentInfo = commentService.findCommentByKPIId(projectBasicConfig,kpi);
    if(mappedCommentInfo==null || mappedCommentInfo.isEmpty())
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ServiceResponse(true, "search not found", mappedCommentInfo));
    }
    return ResponseEntity.status(HttpStatus.OK)
            .body(new ServiceResponse(true, "Found comments", mappedCommentInfo));

}

    @PostMapping("/submitComments")
    public ResponseEntity<ServiceResponse> submitComments( @RequestBody CommentSubmitDTO comment) {

       boolean responseStatus = commentService.submitComment(comment);
        if (responseStatus) {
            return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(responseStatus,
                    "Your Comment has been submitted", comment));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(responseStatus,
                    "issue in comment saving ", comment));
        }

    }




}