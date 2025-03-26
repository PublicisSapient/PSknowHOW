package com.publicissapient.kpidashboard.rally.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RallyTypeDefinitionResponse {
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
        private List<TypeDefinition> results;
    }

    @Data
    public static class TypeDefinition {
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
    }
}
