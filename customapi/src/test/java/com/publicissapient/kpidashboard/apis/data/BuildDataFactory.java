package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.application.Build;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sansharm13
 *
 */
@Slf4j
public class BuildDataFactory {
	private static final String FILE_PATH_BUILD_DATA = "/json/non-JiraProcessors/build_details.json";
	private List<Build> buildDataFactory;
	private ObjectMapper mapper;

	private BuildDataFactory() {
	}

	public static BuildDataFactory newInstance(String filePath) {

		BuildDataFactory factory = new BuildDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static BuildDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_BUILD_DATA : filePath;

			buildDataFactory = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<Build>>() {
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

	public List<Build> getbuildDataList() {
		return buildDataFactory;
	}

}
