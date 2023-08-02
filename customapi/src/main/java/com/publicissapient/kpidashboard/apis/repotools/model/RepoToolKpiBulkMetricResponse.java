package com.publicissapient.kpidashboard.apis.repotools.model;

import lombok.Data;

import java.util.List;

@Data
public class RepoToolKpiBulkMetricResponse {
    private List<List<RepoToolKpiMetricResponse>> values;
}
