package com.publicissapient.kpidashboard.common.model.comment;


import lombok.*;
import org.springframework.data.annotation.Id;


@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentInfo {


   @Id
    private Integer commentId;

    private  String commentBy;

    private String commentOn;

    private String comment;


}

