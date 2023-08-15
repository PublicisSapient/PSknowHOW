package com.publicissapient.kpidashboard.common.model.application;


import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents entity to store details of active itr data fetch
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "sprint_trace_log")
public class SprintTraceLog extends BasicModel {
    private String sprintId;
    private boolean fetchSuccessful;
    private boolean errorInFetch;
    // in millisecond
    private long lastSyncDateTime;
}