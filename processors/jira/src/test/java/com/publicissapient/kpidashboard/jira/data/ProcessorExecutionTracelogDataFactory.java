package com.publicissapient.kpidashboard.jira.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ProcessorExecutionTracelogDataFactory {
    private static final String FILE_PATH_FIELD_MAPPING = "/json/default/processor_execution_tracelog";
    private List<ProcessorExecutionTraceLog> processorExecutionTracelog;
    private ObjectMapper mapper;

    public ProcessorExecutionTracelogDataFactory() {
    }

    public static ProcessorExecutionTracelogDataFactory newInstance(String filePath) {

        ProcessorExecutionTracelogDataFactory factory = new ProcessorExecutionTracelogDataFactory();
        factory.createObjectMapper();
        factory.init(filePath);
        return factory;
    }

    private void init(String filePath) {
        try {

            String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_FIELD_MAPPING : filePath;

            processorExecutionTracelog = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
                    new TypeReference<List<ProcessorExecutionTraceLog>>() {
                    });
        } catch (IOException e) {
            log.error("Error in reading processor execution tracelog from file = " + filePath, e);
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

    public List<ProcessorExecutionTraceLog> getProcessorExecutionTracelog() {
        return processorExecutionTracelog;
    }

}
