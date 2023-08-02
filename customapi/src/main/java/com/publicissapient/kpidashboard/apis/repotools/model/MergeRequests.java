package com.publicissapient.kpidashboard.apis.repotools.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MergeRequests {

    private String id;
    private String link;
    @JsonProperty("time_to_merge")
    private long timeToMerge;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;

}
