package com.publicissapient.kpidashboard.common.model.comments;

import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentsKpiWise {

    private String kpiId;
    private List<CommentsInfo> commentsInfo;

}
