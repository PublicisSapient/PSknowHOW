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
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sansharm13
 *
 */
@Slf4j
public class AccountHierarchyKanbanFilterDataFactory {
	private static final String FILE_PATH = "/json/default/kanban_account_hierarchy_filter_data.json";
	private static final String FILE_PATH_ACCOUNT_HIERARCHIES_FILTER_DATA = "/json/default/account_hierarchy_filter_data.json";
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanban;
	private ObjectMapper mapper = null;

	private AccountHierarchyKanbanFilterDataFactory() {
	}

	public static AccountHierarchyKanbanFilterDataFactory newInstance() {
		return newInstance(null);
	}

	public static AccountHierarchyKanbanFilterDataFactory newInstance(String filePath) {

		AccountHierarchyKanbanFilterDataFactory accountHierarchyDataKanbanDataFactory = new AccountHierarchyKanbanFilterDataFactory();
		accountHierarchyDataKanbanDataFactory.createObjectMapper();
		accountHierarchyDataKanbanDataFactory.init(filePath);
		return accountHierarchyDataKanbanDataFactory;
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH : filePath;

			accountHierarchyDataKanban = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<AccountHierarchyDataKanban>>() {
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

	public List<AccountHierarchyDataKanban> getAccountHierarchyKanbanDataList() {
		return accountHierarchyDataKanban;
	}

}