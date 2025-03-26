package com.publicissapient.kpidashboard.rally.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RallyAllowedValuesResponse {
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
        private List<AllowedValue> results;
    }

    @Data
    public static class AllowedValue {
        @JsonProperty("_ref")
        private String ref;
        @JsonProperty("_refObjectUUID")
        private String refObjectUUID;
        @JsonProperty("_refObjectName")
        private String refObjectName;
        @JsonProperty("_type")
        private String type;
        @JsonProperty("StringValue")
        private String stringValue;
        @JsonProperty("DisplayName")
        private String displayName;
        @JsonProperty("Name")
        private String name;

        public String getDisplayValue() {
            if (displayName != null && !displayName.isEmpty()) {
                return displayName;
            }
            if (name != null && !name.isEmpty()) {
                return name;
            }
            return stringValue;
        }

        public String getStringValue() {
            if (stringValue != null && !stringValue.isEmpty()) {
                return stringValue;
            }
            if (name != null && !name.isEmpty()) {
                return name;
            }
            return displayName;
        }
    }
}
