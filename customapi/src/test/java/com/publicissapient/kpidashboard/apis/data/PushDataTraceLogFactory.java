/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataTraceLog;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PushDataTraceLogFactory {

	private static final String FILE_PATH_ACCOUNT_HIERARCHIES = "/json/pushdata/push_data_trace_log.json";
	private List<PushDataTraceLog> pushDataTraceLogs;
	private ObjectMapper mapper;

	private PushDataTraceLogFactory() {
	}

	public static PushDataTraceLogFactory newInstance(String filePath) {

		PushDataTraceLogFactory factory = new PushDataTraceLogFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static PushDataTraceLogFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_ACCOUNT_HIERARCHIES : filePath;

			pushDataTraceLogs = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<PushDataTraceLog>>() {
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

	public List<PushDataTraceLog> getPushDataTraceLog() {
		return pushDataTraceLogs;
	}
}
