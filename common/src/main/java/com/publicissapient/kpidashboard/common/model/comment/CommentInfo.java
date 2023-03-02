package com.publicissapient.kpidashboard.common.model.comment;

import java.util.Date;


import lombok.*;
//import org.springframework.data.annotation.GeneratedValue;
import org.springframework.data.annotation.Id;


@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentInfo {


   // @GeneratedValue
   @Id
    private Integer commentId;

    private  String commentBy;

    private Date commentOn;

    private String comment;


}

