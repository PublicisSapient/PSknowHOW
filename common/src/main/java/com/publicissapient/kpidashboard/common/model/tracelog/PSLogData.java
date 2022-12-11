package com.publicissapient.kpidashboard.common.model.tracelog;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PSLogData {
    private String projectName;
    private String processorStartTime;
    private String fieldMappingToDB;
    private String metaDataToDB;
    private String boardId;
    private String sprintId;
    private String totalIssues;
    private String totalSavedIssues;
    Map<String,String> issueMap;
    private String url;
    private String jql;
    private String errorMessage;
    private String timeElapsed;
    private String issueNumber;
    private String userTimeZone;
    List<String> sprintListFetched;
    List<String> sprintListSaved;
    List<String> epicListFetched;
    String fetchedIssues;
    private ProcessorExecutionTraceLog processorExecutionTraceLog;

}
