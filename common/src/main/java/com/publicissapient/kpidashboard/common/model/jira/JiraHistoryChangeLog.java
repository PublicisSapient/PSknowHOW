package com.publicissapient.kpidashboard.common.model.jira;

import lombok.*;
import org.joda.time.DateTime;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JiraHistoryChangeLog {
    private String changedFrom;
    private String changedTo;
    private DateTime updatedOn;
}
