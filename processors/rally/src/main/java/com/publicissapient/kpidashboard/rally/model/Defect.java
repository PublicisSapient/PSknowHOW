package com.publicissapient.kpidashboard.rally.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Defect {
    @JsonProperty("FormattedID")
    private String formattedID;
    @JsonProperty("Name")
    private String name;
    private String requirementRef; // Linked HierarchicalRequirement
    @JsonProperty("_ref")
    private String ref;
    @JsonProperty("_refObjectName")
    private String refObjectName;
    @JsonProperty("Owner")
    private Owner owner;
    @JsonProperty("ScheduleState")
    private String scheduleState;
    @JsonProperty("PlanEstimate")
    private Double planEstimate;
    @JsonProperty("_type")
    private String type;
    @JsonProperty("Iteration")
    private Iteration iteration;
    @JsonProperty("Project")
    private Project project;
    @JsonProperty("CreationDate")
    private String creationDate;
    @JsonProperty("LastUpdateDate")
    private String lastUpdateDate;
    @JsonProperty("ObjectID")
    private String objectID;
    @JsonProperty("Requirement")
    private HierarchicalRequirement requirement; // Link to the hierarchical requirement
    // Add a field to store linked hierarchical requirements
    private List<HierarchicalRequirement> linkedRequirements;

}