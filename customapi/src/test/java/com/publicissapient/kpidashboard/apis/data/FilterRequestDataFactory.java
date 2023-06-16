package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.apis.model.AccountFilterRequest;
import com.publicissapient.kpidashboard.apis.model.AccountFilterResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilterRequestDataFactory {

	private static final String FILE_PATH_FILTER_REQUEST = "/json/default/filter_Request.json";
	private AccountFilterRequest accountFilterRequest;
	private List<AccountFilterResponse> filterDataList;
	private ObjectMapper mapper = null;

	public static FilterRequestDataFactory newInstance(String filePath) {

		FilterRequestDataFactory filterRequestDataFactory = new FilterRequestDataFactory();
		filterRequestDataFactory.createObjectMapper();
		filterRequestDataFactory.init(StringUtils.isEmpty(filePath) ? FILE_PATH_FILTER_REQUEST : filePath);
		return filterRequestDataFactory;
	}

	public static FilterRequestDataFactory newInstance() {
		return newInstance(null);
	}

	private void init(String filePath) {
		try {
			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_FILTER_REQUEST : filePath;

			accountFilterRequest = mapper.readValue(TypeReference.class.getResourceAsStream(filePath),
					AccountFilterRequest.class);
		} catch (IOException e) {
			log.error("Error in reading kpi request from file = " + filePath, e);
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

	public AccountFilterRequest getFilterRequest() {
		return accountFilterRequest;
	}

}
