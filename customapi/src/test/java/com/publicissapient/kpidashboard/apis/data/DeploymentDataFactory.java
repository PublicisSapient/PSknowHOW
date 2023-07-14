package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.application.Deployment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeploymentDataFactory {

	private static final String FILE_PATH_BUILD_DATA = "/json/non-JiraProcessors/deployment.json";
	private List<Deployment> deploymentList;
	private ObjectMapper mapper;

	private DeploymentDataFactory() {
	}

	public static DeploymentDataFactory newInstance(String filePath) {

		DeploymentDataFactory factory = new DeploymentDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static DeploymentDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_BUILD_DATA : filePath;

			deploymentList = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<Deployment>>() {
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

	public List<Deployment> getDeploymentDataList() {
		return deploymentList;
	}
}
