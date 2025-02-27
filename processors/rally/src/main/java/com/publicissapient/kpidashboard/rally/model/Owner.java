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

@Data
public class Owner {
    @JsonProperty("_rallyAPIMajor")
    private String _rallyAPIMajor;
    @JsonProperty("_rallyAPIMinor")
    private String _rallyAPIMinor;
    @JsonProperty("_ref")
    private String _ref;
    @JsonProperty("_refObjectUUID")
    private String _refObjectUUID;
    @JsonProperty("_objectVersion")
    private String _objectVersion;
    @JsonProperty("_refObjectName")
    private String _refObjectName;
    @JsonProperty("_type")
    private String _type;
}