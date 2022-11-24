package com.publicissapient.kpidashboard.common.model.jira;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class SprintIssue {
    private String number;
    private String originBoardId;
    private String priority;
    private String status;
    private String typeName;
    private Double storyPoints;
    private Double originalEstimate;
    private Double remainingEstimate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SprintIssue sprintDetails = (SprintIssue) o;
        return Objects.equals(number, sprintDetails.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
