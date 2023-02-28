package com.publicissapient.kpidashboard.common.model.comment;

import lombok.Data;

import java.util.Date;

@Data
public class CommentInfo {

    private String docId;

    private  String commentBy;

    private Date commentOn;

    private String comment;
}
