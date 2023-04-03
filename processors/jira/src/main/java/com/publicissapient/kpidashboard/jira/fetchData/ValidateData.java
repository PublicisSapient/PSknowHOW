package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import lombok.extern.slf4j.Slf4j;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
public class ValidateData {

    public void check(int total, int savedCount, boolean processorFetchingComplete, PSLogData psLogData){
        boolean isAttemptSuccess=isAttemptSuccess(total,savedCount,processorFetchingComplete,psLogData);
        if (!isAttemptSuccess) {
            psLogData.setProjectExecutionStatus(String.valueOf(isAttemptSuccess));
            log.error("Error in Fetching Issues through JQL", kv(CommonConstant.PSLOGDATA, psLogData));
        }
    }

    private boolean isAttemptSuccess(int total, int savedCount, boolean processorFetchingComplete, PSLogData psLogData) {
        psLogData.setTotalFetchedIssues(String.valueOf(total));
        psLogData.setTotalSavedIssues(String.valueOf(savedCount));
        return savedCount > 0 && total == savedCount && processorFetchingComplete;
    }

}
