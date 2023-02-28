package com.publicissapient.kpidashboard.common.model.comment;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "kpi_comments")
public class KPIComments extends BasicModel {

    private String projectBasicConfig;
    private List<CommentKpiWise> commentKpiWise;



}
