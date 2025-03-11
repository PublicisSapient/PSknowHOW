package com.publicissapient.kpidashboard.rally.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
public class RallyStateResponse {
    @JsonProperty("QueryResult")
    private QueryResult queryResult;

    @Data
    public static class QueryResult {
        @JsonProperty("_rallyAPIMajor")
        private String rallyAPIMajor;
        @JsonProperty("_rallyAPIMinor")
        private String rallyAPIMinor;
        @JsonProperty("Errors")
        private List<String> errors;
        @JsonProperty("Warnings")
        private List<String> warnings;
        @JsonProperty("TotalResultCount")
        private int totalResultCount;
        @JsonProperty("StartIndex")
        private int startIndex;
        @JsonProperty("PageSize")
        private int pageSize;
        @JsonProperty("Results")
        private List<State> results;
    }

    @Data
    public static class State {
        @JsonProperty("_rallyAPIMajor")
        private String rallyAPIMajor;
        @JsonProperty("_rallyAPIMinor")
        private String rallyAPIMinor;
        @JsonProperty("_ref")
        private String ref;
        @JsonProperty("_refObjectUUID")
        private String refObjectUUID;
        @JsonProperty("_refObjectName")
        private String refObjectName;
        @JsonProperty("_type")
        private String type;
        @JsonProperty("Name")
        private String name;
        @JsonProperty("OrderIndex")
        private int orderIndex;
        @JsonProperty("Enabled")
        private boolean enabled;
        @JsonProperty("StateCategory")
        private StateCategory stateCategory;
    }
    
    @Data
    public static class StateCategory {
        @JsonProperty("_rallyAPIMajor")
        private String rallyAPIMajor;
        @JsonProperty("_rallyAPIMinor")
        private String rallyAPIMinor;
        @JsonProperty("_ref")
        private String ref;
        @JsonProperty("_type")
        private String type;
        @JsonProperty("Name")
        private String name;
        @JsonProperty("TypeName")
        private String typeName;
    }
}
