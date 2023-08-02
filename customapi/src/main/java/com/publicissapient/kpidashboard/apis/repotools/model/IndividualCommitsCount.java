package com.publicissapient.kpidashboard.apis.repotools.model;

import lombok.Data;

@Data
public class IndividualCommitsCount {

    private String committerEmail;
    private long count;
    private double grade;

}
