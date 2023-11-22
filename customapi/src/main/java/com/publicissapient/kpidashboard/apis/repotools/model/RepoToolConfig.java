/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
    @SerializedName("is_cloneable")
    private Boolean isCloneable;
}
