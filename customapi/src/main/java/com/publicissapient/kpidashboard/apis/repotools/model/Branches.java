package com.publicissapient.kpidashboard.apis.repotools.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Branches {

    @JsonProperty("branch__name")
    private String branchName;
    private String name;
    private long count;
    @JsonProperty("merge_requests")
    private List<MergeRequests> mergeRequestList;
    private long mergeRequests;
    private double grade;
    private double average;
}
