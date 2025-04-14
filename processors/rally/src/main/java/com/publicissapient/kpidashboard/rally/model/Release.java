package com.publicissapient.kpidashboard.rally.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Release {
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

    @JsonProperty("_objectVersion")
    private String objectVersion;

    @JsonProperty("CreationDate")
    private String creationDate;

    @JsonProperty("_CreatedAt")
    private String createdAt;

    @JsonProperty("ObjectID")
    private Long objectID;

    @JsonProperty("ObjectUUID")
    private String objectUUID;

    @JsonProperty("VersionId")
    private String versionId;

    @JsonProperty("Accepted")
    private Double accepted;

    @JsonProperty("CascadedToChildren")
    private Boolean cascadedToChildren;

    @JsonProperty("GrossEstimateConversionRatio")
    private Double grossEstimateConversionRatio;

    @JsonProperty("LastUpdateDate")
    private String lastUpdateDate;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("PlanEstimate")
    private Double planEstimate;

    @JsonProperty("PlannedVelocity")
    private Double plannedVelocity;

    @JsonProperty("ReleaseDate")
    private String releaseDate;

    @JsonProperty("ReleaseStartDate")
    private String releaseStartDate;

    @JsonProperty("State")
    private String state;

    @JsonProperty("SyncedWithParent")
    private Boolean syncedWithParent;

    @JsonProperty("TaskActualTotal")
    private Double taskActualTotal;

    @JsonProperty("TaskEstimateTotal")
    private Double taskEstimateTotal;

    @JsonProperty("TaskRemainingTotal")
    private Double taskRemainingTotal;

    @JsonProperty("Theme")
    private String theme;

    @JsonProperty("Version")
    private String version;

    @JsonProperty("WorkProducts")
    private WorkProducts workProducts;

    @JsonProperty("Errors")
    private List<String> errors;

    @JsonProperty("Warnings")
    private List<String> warnings;
}