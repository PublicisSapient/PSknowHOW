package com.publicissapient.kpidashboard.common.model.comment;

import lombok.Data;

import java.util.List;

@Data
public class CommentKpiWise {

    private String kpiId;
    private List<CommentInfo> commentInfo;

}
