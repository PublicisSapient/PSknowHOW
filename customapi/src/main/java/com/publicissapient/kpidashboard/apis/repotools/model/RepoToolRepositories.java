package com.publicissapient.kpidashboard.apis.repotools.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RepoToolRepositories {

    private String name;
    private String repository;
    private double average;
    @JsonProperty("commit_count")
    private long commitCount;
    private double repositoryGrade;
    private List<IndividualCommitsCount> individualCommitsCount;
    private List<Branches> branchesCommitsCount;
    private List<Branches> branches;
    private String filteredBranch;

}
