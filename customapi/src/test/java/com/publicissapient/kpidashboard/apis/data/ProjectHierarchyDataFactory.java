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

