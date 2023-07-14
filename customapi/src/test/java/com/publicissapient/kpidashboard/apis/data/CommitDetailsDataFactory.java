package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sansharm13
 *
 */
@Slf4j
public class CommitDetailsDataFactory {
	private static final String FILE_PATH_PROCESSOR_ITEMS_DATA = "/json/non-JiraProcessors/commit_details.json";
	private List<CommitDetails> commitDetailsDataFactory;
	private ObjectMapper mapper;

	private CommitDetailsDataFactory() {
	}

	public static CommitDetailsDataFactory newInstance(String filePath) {

		CommitDetailsDataFactory factory = new CommitDetailsDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static CommitDetailsDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_PROCESSOR_ITEMS_DATA : filePath;

			commitDetailsDataFactory = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<CommitDetails>>() {
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

	public List<CommitDetails> getcommitDetailsList() {
		return commitDetailsDataFactory;
	}

}
