package com.publicissapient.kpidashboard.apis.comments.rest;


import com.publicissapient.kpidashboard.apis.jira.rest.JiraController;
import com.publicissapient.kpidashboard.common.model.comment.CommentKpiWise;
import com.publicissapient.kpidashboard.common.model.comment.CommentSubmitDTO;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.comments.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.publicissapient.kpidashboard.common.model.comment.KPIComments;

import java.util.List;


@RestController
@RequestMapping("/comments")
@Slf4j
public class CommentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentController.class);
    @Autowired
    private CommentService commentService;

@GetMapping("/getCommentsByKpiId")
public ResponseEntity<ServiceResponse> getCommentsByKPI(@RequestParam String projectBasicConfig, String kpi) {

    final List<CommentKpiWise> kpiComment = commentService.findCommentByKPIId(projectBasicConfig,kpi);
   // final List<KPIComments> kpiComment1 = commentService.findCommentByKPIIdList(projectBasicConfig,kpi);
    if(kpiComment==null || kpiComment.isEmpty())
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ServiceResponse(true, "search not found", kpiComment));
    }
    return ResponseEntity.status(HttpStatus.OK)
            .body(new ServiceResponse(true, "Found comments", kpiComment));

}

    @PostMapping("/submitComments")
    public ResponseEntity<ServiceResponse> submitComments( @RequestBody CommentSubmitDTO comment) {
       // log.info("creating new request");

       boolean responseStatus = commentService.submitComment(comment);
      //Boolean  responseStatus=true;
        if (responseStatus) {
            return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(responseStatus,
                    "Your Comment has been submitted", comment));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(responseStatus,
                    "issue in comment saving ", comment));
        }

    }




}