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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HierarchicalRequirement {
    @JsonProperty("_ref")
    private String ref;
    @JsonProperty("_refObjectName")
    private String refObjectName;
    @JsonProperty("FormattedID")
    private String formattedID;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Owner")
    private Owner owner;
    @JsonProperty("ScheduleState")
    private String scheduleState;
    @JsonProperty("Blocked")
    private boolean blocked;
    @JsonProperty("PlanEstimate")
    private Double planEstimate;
    @JsonProperty("_type")
    private String type;
    @JsonProperty("Iteration")
    private Iteration iteration;
    @JsonProperty("Project")
    private Project project;
    @JsonProperty("CreationDate")
    private String creationDate;
    @JsonProperty("LastUpdateDate")
    private String lastUpdateDate;
    @JsonProperty("ObjectID")
    private String objectID;
    private String currentIteration;
    private List<String> pastIterations; // Track spillover
    // Add a field to store linked defects
    private List<Defect> linkedDefects = new ArrayList<>();
}