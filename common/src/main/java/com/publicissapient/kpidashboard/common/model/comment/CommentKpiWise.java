package com.publicissapient.kpidashboard.common.model.comment;

import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentKpiWise {

    private String kpiId;
    private List<CommentInfo> commentInfo;

}
