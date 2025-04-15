package com.publicissapient.kpidashboard.apis.mongock.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.jira.ConfigurationTemplateDocument;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationTemplateDataFactory {
	private static final String FILE_PATH_CONFIGURATION_TEMPLATE_LIST = "/json/mongock/default/configuration_template.json";
	private List<ConfigurationTemplateDocument> configurationTemplateList;
	private ObjectMapper mapper;

	private ConfigurationTemplateDataFactory() {
	}

	public static ConfigurationTemplateDataFactory newInstance(String filePath) {
		ConfigurationTemplateDataFactory factory = new ConfigurationTemplateDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static ConfigurationTemplateDataFactory newInstance() {
		return newInstance(null);
	}

	private void init(String filePath) {
		try {
			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_CONFIGURATION_TEMPLATE_LIST : filePath;

			// Get the resource stream properly
			InputStream inputStream = getClass().getResourceAsStream(resultPath);
			if (inputStream == null) {
				throw new IllegalArgumentException("Resource not found: " + resultPath);
			}
			configurationTemplateList = mapper.readValue(inputStream,
					new TypeReference<List<ConfigurationTemplateDocument>>() {
					});
		} catch (IOException e) {
			log.error("Error in reading configuration templates from: " + filePath, e);
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

	public List<ConfigurationTemplateDocument> getConfigurationTemplateList() {
		return configurationTemplateList;
	}
}
