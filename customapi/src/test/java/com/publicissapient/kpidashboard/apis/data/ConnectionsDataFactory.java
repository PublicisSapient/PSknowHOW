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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.connection.Connection;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */

@Slf4j
public class ConnectionsDataFactory {

	private static final String FILE_PATH_CONNECTIONS = "/json/default/connections.json";
	private List<Connection> connections;
	private ObjectMapper mapper = null;

	private ConnectionsDataFactory() {
	}

	public static ConnectionsDataFactory newInstance() {
		return newInstance(null);
	}

	public static ConnectionsDataFactory newInstance(String filePath) {

		ConnectionsDataFactory connectionsDataFactory = new ConnectionsDataFactory();
		connectionsDataFactory.createObjectMapper();
		connectionsDataFactory.init(filePath);
		return connectionsDataFactory;
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_CONNECTIONS : filePath;

			connections = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<Connection>>() {
					});
		} catch (IOException e) {
			log.error("Error in reading kpi request from file = " + filePath, e);
		}
	}

	private ObjectMapper createObjectMapper() {

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

		return mapper;
	}

	public List<Connection> getConnections() {
		return connections;
	}

	public Connection findConnectionById(String id) {

		return connections.stream().filter(connection -> connection.getId().toString().equals(id)).findFirst()
				.orElse(null);
	}

	public List<Connection> findConnectionsByType(String type) {

		return connections.stream().filter(connection -> connection.getType().equals(type))
				.collect(Collectors.toList());
	}
}
