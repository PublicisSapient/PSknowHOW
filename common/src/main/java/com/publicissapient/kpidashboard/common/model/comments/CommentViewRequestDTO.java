package com.publicissapient.kpidashboard.common.model.comments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentViewRequestDTO {

    private List<String> node;
    private String level;
    private String sprintId;
    private List<String> kpiIds;
}
