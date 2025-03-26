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

/**
 * Represents a field change in a Rally issue.
 * Handles both standard field changes and reference field changes (_ref URLs).
 */
@Data
public class RallyIssueField {
    @JsonProperty("_rallyAPIMajor")
    private Integer rallyAPIMajor;
    
    @JsonProperty("_rallyAPIMinor")
    private Integer rallyAPIMinor;
    
    @JsonProperty("_type")
    private String type;
    
    @JsonProperty("Name")
    private String field;
    
    @JsonProperty("OldValue")
    private String fromString;
    
    @JsonProperty("NewValue")
    private String toString;
    
    @JsonProperty("OldValueRef")
    private String from;
    
    @JsonProperty("NewValueRef")
    private String to;
}
