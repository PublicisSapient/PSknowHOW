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
package com.publicissapient.kpidashboard.apis.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author purgupta2
 */
@Slf4j
public class ProjectHierarchyDataFactory {


    private static final String FILE_PATH_ORGANIZATION_HIERARCHY = "/json/default/project_hierarchy.json";

    private List<ProjectHierarchy> organizationHierarchies;

    private ObjectMapper mapper;

    private ProjectHierarchyDataFactory() {
    }

    public static ProjectHierarchyDataFactory newInstance(String filePath) {
        ProjectHierarchyDataFactory factory = new ProjectHierarchyDataFactory();
        factory.createObjectMapper();
        factory.init(filePath);
        return factory;
    }

    public static ProjectHierarchyDataFactory newInstance() {

        return newInstance(null);
    }

    private void init(String filePath) {
        try {
            String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_ORGANIZATION_HIERARCHY : filePath;

            organizationHierarchies = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
                    new TypeReference<List<ProjectHierarchy>>() {
                    });
        } catch (Exception e) {
            log.error("Error in reading organization hierarchy from file = " + filePath, e);
        }
    }

    private void createObjectMapper() {

        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
    }

    public List<ProjectHierarchy> getProjectHierarchies() {
        return organizationHierarchies;
    }

}

