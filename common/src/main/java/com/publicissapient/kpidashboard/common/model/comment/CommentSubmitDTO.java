package com.publicissapient.kpidashboard.common.model.comment;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentSubmitDTO {


    private String projectBasicConfig;
    private List<CommentKpiWise> commentKpiWise;



}
