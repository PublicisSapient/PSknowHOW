package com.publicissapient.kpidashboard.common.model.jira;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "jira_issue_release_status")
public class JiraIssueReleaseStatus {

    private String basicProjectConfigId;
    private Map<Long, String> listOfTodos;
    private Map<Long, String> listOfInProgress;
    private Map<Long, String> listOfClosed;

}
