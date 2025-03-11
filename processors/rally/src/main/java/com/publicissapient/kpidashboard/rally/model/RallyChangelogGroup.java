package com.publicissapient.kpidashboard.rally.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RallyChangelogGroup {
    @JsonProperty("_rallyAPIMajor")
    private Integer rallyAPIMajor;
    
    @JsonProperty("_rallyAPIMinor")
    private Integer rallyAPIMinor;
    
    @JsonProperty("Errors")
    private List<String> errors;
    
    @JsonProperty("Warnings")
    private List<String> warnings;
    
    @JsonProperty("CreationDate")
    private String created;
    
    @JsonProperty("Owner")
    private Owner author;
    
    @JsonProperty("Results")
    private List<RallyIssueField> items;
}
