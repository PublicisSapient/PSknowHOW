package com.publicissapient.kpidashboard.common.model.comment;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentInfo {


    @Id  @Field("_id") @Indexed(unique = true)
    public Integer commentId;

    private  String commentBy;

    @CreatedDate
    private Date commentOn;

    private String comment;


}

