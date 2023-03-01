package com.publicissapient.kpidashboard.common.model.comment;


import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentSubmitDTO{

    private String node;
    private String projectBasicConfig;
    private List<CommentKpiWise> commentKpiWise;


}
