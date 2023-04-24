package com.publicissapient.kpidashboard.azure.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;

@Slf4j
public class FieldMappingDataFactory {

    private static final String FILE_PATH_FIELD_MAPPING = "/json/default/field_mappings.json";
    private List<FieldMapping> fieldMappings;
    private ObjectMapper mapper;

    public FieldMappingDataFactory() {
    }

    public static FieldMappingDataFactory newInstance(String filePath) {

        FieldMappingDataFactory factory = new FieldMappingDataFactory();
        factory.createObjectMapper();
        factory.init(filePath);
        return factory;
    }

    private void init(String filePath) {
        try {

            String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_FIELD_MAPPING : filePath;

            fieldMappings = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
                    new TypeReference<List<FieldMapping>>() {
                    });
        } catch (IOException e) {
            log.error("Error in reading field mappings from file = " + filePath, e);
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

    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    public FieldMapping findById(String id) {

        return fieldMappings.stream().filter(fieldMapping -> fieldMapping.getId().toHexString().equals(id)).findFirst()
                .orElse(null);
    }

    public FieldMapping findByToolConfigId(String toolConfigId) {

        return fieldMappings.stream()
                .filter(fieldMapping -> fieldMapping.getProjectToolConfigId().toHexString().equals(toolConfigId))
                .findFirst().orElse(null);
    }

    public FieldMapping findByBasicProjectConfigId(String basicProjectConfigId) {

        return fieldMappings.stream().filter(
                        fieldMapping -> fieldMapping.getBasicProjectConfigId().toHexString().equals(basicProjectConfigId))
                .findFirst().orElse(null);
    }
}
