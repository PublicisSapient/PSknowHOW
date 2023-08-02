package com.publicissapient.kpidashboard.apis.repotools.model;

import com.google.gson.annotations.SerializedName;
import com.publicissapient.kpidashboard.common.model.ToolCredential;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepoToolConfig {
    @SerializedName("name")
    private String name;
    @SerializedName("isNew")
    private Boolean isNew;
    @SerializedName("master_system_id")
    private String masterSystemId;
    @SerializedName("http_url")
    private String httpUrl;
    @SerializedName("provider")
    private String provider;
    @SerializedName("ssh_url")
    private String sshUrl;
    @SerializedName("default_branch")
    private String defaultBranch;
    @SerializedName("project_code")
    private String projectCode;
    @SerializedName("first_scan_from")
    private String firstScanFrom;
    @SerializedName("scanning_account")
    private ToolCredential scanningAccount;
    @SerializedName("scanning_branches")
    private List<String> scanningBranches;
}
