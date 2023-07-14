/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sansharm13
 *
 */
@Slf4j
public class SonarDeatailsDataFactory {
	private static final String FILE_PATH_SONAR_DETAILS_DATA = "/json/non-JiraProcessors/sonar_details.json";
	private List<SonarDetails> sonarDeatailsDataFactory;
	private ObjectMapper mapper;

	private SonarDeatailsDataFactory() {
	}

	public static SonarDeatailsDataFactory newInstance(String filePath) {

		SonarDeatailsDataFactory factory = new SonarDeatailsDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static SonarDeatailsDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_SONAR_DETAILS_DATA : filePath;

			sonarDeatailsDataFactory = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<SonarDetails>>() {
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

	public List<SonarDetails> getSonarDetailsList() {
		return sonarDeatailsDataFactory;
	}

}
