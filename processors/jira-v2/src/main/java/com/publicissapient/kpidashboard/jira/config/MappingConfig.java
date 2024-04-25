package com.publicissapient.kpidashboard.jira.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;

@Configuration
public class MappingConfig {

    private Map<String, String> fieldMappings;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.fieldMappings = objectMapper.readValue(
                new ClassPathResource("mapping.json").getInputStream(),
                new TypeReference<Map<String, String>>() {});
    }

    public String getDbFieldName(String jiraFieldName) {
        return fieldMappings.get(jiraFieldName);
    }
}
