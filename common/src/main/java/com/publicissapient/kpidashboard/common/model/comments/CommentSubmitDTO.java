package com.publicissapient.kpidashboard.common.model.comments;


import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentSubmitDTO{

    private String node;
    private String projectBasicConfig;
    private List<CommentsKpiWise> commentsKpiWise;


}
