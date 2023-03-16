package com.publicissapient.kpidashboard.common.model.jira;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JiraHistoryChangeLog {
    private String changedFrom;
    private String changedTo;
    private LocalDateTime updatedOn;
}
