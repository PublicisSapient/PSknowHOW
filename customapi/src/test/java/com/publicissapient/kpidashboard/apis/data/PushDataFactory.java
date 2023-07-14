package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuild;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuildDeployDTO;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushDeploy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PushDataFactory {

	private static final String FILE_PATH_ACCOUNT_HIERARCHIES = "/json/pushdata/builddeploy.json";
	private List<PushBuildDeployDTO> pushBuildDeploy;
	private ObjectMapper mapper;

	private PushDataFactory() {
	}

	public static PushDataFactory newInstance(String filePath) {

		PushDataFactory factory = new PushDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static PushDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_ACCOUNT_HIERARCHIES : filePath;

			pushBuildDeploy = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<PushBuildDeployDTO>>() {
					});
		} catch (IOException e) {
			log.error("Error in json file = " + filePath, e);
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

	public List<PushBuildDeployDTO> getPushBuildDeploy() {
		return pushBuildDeploy;
	}

	public List<PushBuild> getBuild(PushBuildDeploy pushBuildDeploy) {
		return new ArrayList<>(pushBuildDeploy.getBuilds());
	}

	public List<PushDeploy> getDeployments(PushBuildDeploy pushBuildDeploy) {
		return new ArrayList<>(pushBuildDeploy.getDeployments());
	}
}
