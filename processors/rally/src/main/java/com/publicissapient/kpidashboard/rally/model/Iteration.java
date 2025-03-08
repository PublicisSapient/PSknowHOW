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

package com.publicissapient.kpidashboard.rally.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
    public class Iteration {
        @JsonProperty("_ref")
        private String ref;
        @JsonProperty("_refObjectName")
        private String refObjectName;
        @JsonProperty("Name")
        private String name;
        @JsonProperty("PlanEstimate")
        private Double planEstimate;
        @JsonProperty("StartDate")
        private String startDate;
        @JsonProperty("EndDate")
        private String endDate;
        @JsonProperty("State")
        private String state;
        @JsonProperty("PlannedVelocity")
        private Double plannedVelocity;
        @JsonProperty("Workspace")
        private Workspace workspace;
        @JsonProperty("Project")
        private Project project;
        @JsonProperty("RevisionHistory")
        private RevisionHistory revisionHistory;
        @JsonProperty("UserIterationCapacities")
        private UserIterationCapacity userIterationCapacities;
        @JsonProperty("WorkProducts")
        private WorkProducts workProducts;
        @JsonProperty("LastUpdateDate")
        private String lastUpdateDate;
        @JsonProperty("TaskActualTotal")
        private String taskActualTotal;
        @JsonProperty("TaskEstimateTotal")
        private String taskEstimateTotal;
        @JsonProperty("TaskRemainingTotal")
        private String taskRemainingTotal;
        @JsonProperty("ObjectID")
        private String objectID;

    }
