package com.publicissapient.kpidashboard.rally.model;

import lombok.Data;

@Data
public class RallyArtifact {
    private String id;
    private String rallyAPIMajor;
    private String rallyAPIMinor;
    private String ref;
    private String refObjectUUID;
    private String refObjectName;
    private String type;
    private Iteration iteration;
    private Release release;
    private Defect defect;
    private HierarchicalRequirement hierarchicalRequirement;
    private boolean isSpilledOver;
    private boolean isInBacklog;
}