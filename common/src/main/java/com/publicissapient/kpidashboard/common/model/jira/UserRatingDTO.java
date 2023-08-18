package com.publicissapient.kpidashboard.common.model.jira;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserRatingDTO {
    private Integer rating;
    private String userName;
    private String userId;
}
