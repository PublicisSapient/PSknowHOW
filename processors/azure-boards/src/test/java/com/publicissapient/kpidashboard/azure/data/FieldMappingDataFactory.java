package com.publicissapient.kpidashboard.azure.data;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FieldMappingDataFactory {

	private static final String FILE_PATH_FIELD_MAPPING = "/onlinedata/azure/scrumfieldmapping.json";
	private FieldMapping fieldMappings;
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
					new TypeReference<FieldMapping>() {
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

	public FieldMapping getFieldMappings() {
		return fieldMappings;
	}

}
