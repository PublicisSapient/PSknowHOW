package com.publicissapient.kpidashboard.common.model.kpicommentshistory;

import com.publicissapient.kpidashboard.common.model.comment.CommentKpiWise;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "kpi_comments_history")
public class KpiCommentsHistory extends BasicModel {
    private String node;
    private String projectBasicConfig;
    private List<CommentKpiWise> commentKpiWise;


}
