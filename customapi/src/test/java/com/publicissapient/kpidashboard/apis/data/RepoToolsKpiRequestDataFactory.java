package com.publicissapient.kpidashboard.apis.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiBulkMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
public class RepoToolsKpiRequestDataFactory {

    private static final String FILE_PATH_PROCESSOR_ITEMS_DATA = "/json/non-JiraProcessors/repo_tools_response.json";
    private List<RepoToolKpiMetricResponse> repoToolsKpiRequestDataFactory;
    private ObjectMapper mapper;

    private RepoToolsKpiRequestDataFactory() {
    }

    public static RepoToolsKpiRequestDataFactory newInstance(String filePath) {

        RepoToolsKpiRequestDataFactory factory = new RepoToolsKpiRequestDataFactory();
        factory.createObjectMapper();
        factory.init(filePath);
        return factory;
    }

    public static RepoToolsKpiRequestDataFactory newInstance() {

        return newInstance(null);
    }

    private void init(String filePath) {
        try {

            String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_PROCESSOR_ITEMS_DATA : filePath;

            RepoToolKpiBulkMetricResponse repoToolKpiBulkMetricResponse = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
                    new TypeReference<RepoToolKpiBulkMetricResponse>() {
                    });
            repoToolsKpiRequestDataFactory = repoToolKpiBulkMetricResponse.getValues().get(0);
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

    public List<RepoToolKpiMetricResponse> getRepoToolsKpiRequest() {
        return repoToolsKpiRequestDataFactory;
    }

}
