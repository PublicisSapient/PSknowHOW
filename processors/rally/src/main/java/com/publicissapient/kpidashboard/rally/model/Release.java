package com.publicissapient.kpidashboard.rally.model;

import lombok.Data;

@Data
public class Release {
    private String rallyAPIMajor;
    private String rallyAPIMinor;
    private String ref;
    private String refObjectUUID;
    private String refObjectName;
    private String releaseStartDate;
    private String releaseEndDate;
}