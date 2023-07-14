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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */

@Slf4j
public class KpiRequestFactory {

	private static final String FILE_PATH_KPI_REQUEST = "/json/default/kpi_request.json";
	private static final String FILE_PATH_KPI_LIST = "/json/default/kpi_list.json";

	private ObjectMapper mapper = null;

	private KpiRequest kpiRequest;
	private List<KpiElement> kpiElements;

	private KpiRequestFactory() {

	}

	public static KpiRequestFactory newInstance(String filePath) {

		KpiRequestFactory kpiRequestFactory = new KpiRequestFactory();
		kpiRequestFactory.createObjectMapper();
		kpiRequestFactory.createKpiList();
		kpiRequestFactory.init(StringUtils.isEmpty(filePath) ? FILE_PATH_KPI_REQUEST : filePath);
		return kpiRequestFactory;
	}

	public static KpiRequestFactory newInstance() {
		return newInstance(null);
	}

	private void init(String filePath) {
		try {
			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_KPI_REQUEST : filePath;

			kpiRequest = mapper.readValue(TypeReference.class.getResourceAsStream(filePath), KpiRequest.class);
		} catch (IOException e) {
			log.error("Error in reading kpi request from file = " + filePath, e);
		}
	}

	private void createKpiList() {
		try {

			kpiElements = mapper.readValue(TypeReference.class.getResourceAsStream(FILE_PATH_KPI_LIST),
					new TypeReference<List<KpiElement>>() {
					});
		} catch (IOException e) {
			e.printStackTrace();
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

	public KpiRequest findKpiRequest(String kpiId) {

		List<KpiElement> kpiList = kpiRequest.getKpiList();

		KpiRequest result = SerializationUtils.clone(kpiRequest);

		if (CollectionUtils.isEmpty(kpiList)) {
			KpiElement kpiElement = findKpi(kpiId);
			if (kpiElement != null) {
				result.setKpiList(Arrays.asList(kpiElement));
			}
		}
		return result;
	}

	private KpiElement findKpi(String kpiId) {
		return CollectionUtils.emptyIfNull(kpiElements).stream().filter(kpi -> kpi.getKpiId().equals(kpiId)).findFirst()
				.orElse(null);

	}
}
