package com.publicissapient.kpidashboard.apis.debbie.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.publicissapient.kpidashboard.common.model.ToolCredential;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DebbieConfig {
    @JsonProperty("name")
    private String name;
    @JsonProperty("isNew")
    private Boolean isNew;
    @JsonProperty("master_system_id")
    private String masterSystemId;
    @JsonProperty("http_url")
    private String httpUrl;
    @JsonProperty("provider")
    private String provider;
    @JsonProperty("ssh_url")
    private String sshUrl;
    @JsonProperty("default_branch")
    private String defaultBranch;
    @JsonProperty("project_code")
    private String projectCode;
    @JsonProperty("scanning_account")
    private ToolCredential scanningAccount;
}
