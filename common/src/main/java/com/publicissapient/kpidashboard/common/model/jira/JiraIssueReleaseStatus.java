package com.publicissapient.kpidashboard.common.model.jira;


import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
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
public class JiraIssueReleaseStatus extends BasicModel {

    private String basicProjectConfigId;
    private Map<Long, String> toDoList;
    private Map<Long, String> inProgressList;
    private Map<Long, String> closedList;

}
