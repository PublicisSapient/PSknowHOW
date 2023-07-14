package com.publicissapient.kpidashboard.azure.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SprintDetailsDataFactory {

	private static final String FILE_PATH_SPRINT_DETAILS = "/json/sprint_details_collection.json";
	private List<SprintDetails> sprintDetails;
	private ObjectMapper mapper;

	private SprintDetailsDataFactory() {
	}

	public static SprintDetailsDataFactory newInstance(String filePath) {

		SprintDetailsDataFactory factory = new SprintDetailsDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static SprintDetailsDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_SPRINT_DETAILS : filePath;

			sprintDetails = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<SprintDetails>>() {
					});
		} catch (IOException e) {
			log.error("Error in reading account hierarchies from file = " + filePath, e);
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

	public List<SprintDetails> getSprintDetails() {
		return sprintDetails;
	}
}
