package com.publicissapient.kpidashboard.common.model.comments;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentViewRequestDTO {

    private List<String> nodes;
    private String level;
    private String nodeChildId;
    private List<String> kpiIds;
    private String commentId;
}
