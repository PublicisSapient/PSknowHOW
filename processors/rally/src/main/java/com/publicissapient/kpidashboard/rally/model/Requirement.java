// Requirement.java
package com.publicissapient.kpidashboard.rally.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Requirement {
    @JsonProperty("_ref")
    private String ref;
    @JsonProperty("_refObjectName")
    private String refObjectName;
    @JsonProperty("FormattedID")
    private String formattedID;
    @JsonProperty("Name")
    private String name;
}