/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.publicissapient.kpidashboard.common.model.application;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/** store all filters needed for a dashboard
 *
 * @author purgupta2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "filters")
public class Filters extends BasicModel {
    private Integer boardId;
    private ProjectTypeSwitch projectTypeSwitch;
    private PrimaryFilter primaryFilter;
    private ParentFilter parentFilter;
    private List<AdditionalFilter> additionalFilters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectTypeSwitch {
        private boolean enabled;
        private boolean visible;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryFilter {
        private String type;
        private DefaultLevel defaultLevel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalFilter {
        private String type;
        private DefaultLevel defaultLevel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultLevel {
        private String labelName;
        private String sortBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentFilter {
        private String labelName;
        private String emittedLevel;
    }
}
