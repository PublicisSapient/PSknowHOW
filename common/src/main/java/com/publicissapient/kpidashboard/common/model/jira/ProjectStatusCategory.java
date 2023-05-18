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
@Document(collection = "project_status_categories")
public class ProjectStatusCategory {

    private String basicProjectConfigId;
    private Map<Long, String> listOfTodos;
    private Map<Long, String> listOfInProgress;
    private Map<Long, String> listOfClosed;

}
