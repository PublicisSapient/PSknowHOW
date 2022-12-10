package com.publicissapient.kpidashboard.common.model.tracelog;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PSLogData {
    private String projectName;
    private String processorStartTime;
    private String fieldMappingToDB;
    private String metaDataToDB;
    private String boardId;
    private String totalIssues;
    private String url;
    private String jql;
    private String errorMessage;
    private String timeElapsed;
}
