package com.publicissapient.kpidashboard.common.model.comments;


import org.springframework.data.annotation.Id;

import lombok.*;


@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentsInfo {

    @Id
    private Integer commentId;

    private  String commentBy;

    private String commentOn;

    private String comment;

}

