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
import com.publicissapient.kpidashboard.common.model.application.KpiCategory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Slf4j
public class KpiCategoryDataFactory {

	private static final String FILE_PATH_KPI_LIST = "/json/default/kpi_category.json";
	private List<KpiCategory> kpiCategoryList;
	private ObjectMapper mapper;

	private KpiCategoryDataFactory() {
	}

	public static KpiCategoryDataFactory newInstance(String filePath) {

		KpiCategoryDataFactory factory = new KpiCategoryDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static KpiCategoryDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_KPI_LIST : filePath;

			kpiCategoryList = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<KpiCategory>>() {
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

	public List<KpiCategory> getKpiCategoryList() {
		return kpiCategoryList;
	}

}
