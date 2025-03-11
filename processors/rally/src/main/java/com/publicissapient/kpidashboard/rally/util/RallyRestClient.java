package com.publicissapient.kpidashboard.rally.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.RallyTypeDefinitionResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RallyRestClient {
    private static final String BASE_URL = "https://rally1.rallydev.com/slm/webservice/v2.0";
    private static final String API_KEY_HEADER = "zsessionid";
    private static final String WORKSPACE_PATH = "/workspace";
    private static final String PROJECT_PATH = "/project";
    private static final String TYPEDEFINITION_PATH = "/typedefinition";
    private static final String ALLOWED_VALUES_PATH = "/allowedValues";
    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private RestTemplate restTemplate;

    public RallyRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    public <T> ResponseEntity<T> get(String url, ProjectConfFieldMapping projectConfig, Class<T> responseType) throws JsonProcessingException {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (projectConfig.getProjectToolConfig() != null && projectConfig.getProjectToolConfig().getConnectionId() != null) {
                Connection connection = connectionRepository.findById(projectConfig.getProjectToolConfig().getConnectionId()).orElse(null);
                
                if (connection != null && connection.getAccessToken() != null) {
                    headers.set(API_KEY_HEADER, connection.getAccessToken());
                    headers.set("Accept", "application/json");
                    headers.set("Content-Type", "application/json");
                    
                    log.debug("Making Rally API request to URL: {} with headers: {}", url, headers);
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    ResponseEntity<String> rawResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                    
                    if (rawResponse != null && rawResponse.getBody() != null) {
                        log.debug("Raw Rally API response: {}", rawResponse.getBody());
                        
                        ObjectMapper objectMapper = new ObjectMapper();
                        T parsedResponse = objectMapper.readValue(rawResponse.getBody(), responseType);
                        
                        if (parsedResponse instanceof RallyTypeDefinitionResponse) {
                            RallyTypeDefinitionResponse response = (RallyTypeDefinitionResponse) parsedResponse;
                            if (response.getQueryResult() != null && !response.getQueryResult().getErrors().isEmpty()) {
                                log.error("Rally API returned errors: {}", response.getQueryResult().getErrors());
                                throw new RuntimeException("Rally API returned errors: " + response.getQueryResult().getErrors());
                            }
                        }
                        
                        log.debug("Successfully parsed Rally API response to type: {}", responseType.getSimpleName());
                        return ResponseEntity.ok(parsedResponse);
                    } else {
                        log.warn("Received null response or body from Rally API");
                        return null;
                    }
                } else {
                    log.error("No access token found for connection ID: {}", projectConfig.getProjectToolConfig().getConnectionId());
                    return null;
                }
            } else {
                log.error("Invalid project tool config or connection ID");
                return null;
            }
        } catch (Exception e) {
            log.error("Error making Rally API request to URL: " + url, e);
            throw e;
        }
    }

    public <T> ResponseEntity<T> get(String url, ProjectConfFieldMapping projectConfig, ParameterizedTypeReference<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (projectConfig.getProjectToolConfig() != null && projectConfig.getProjectToolConfig().getConnectionId() != null) {
                Connection connection = connectionRepository.findById(projectConfig.getProjectToolConfig().getConnectionId()).orElse(null);
                
                if (connection != null && connection.getAccessToken() != null) {
                    headers.set(API_KEY_HEADER, connection.getAccessToken());
                    headers.set("Accept", "application/json");
                    headers.set("Content-Type", "application/json");
                    
                    log.debug("Making Rally API request to URL: {} with headers: {}", url, headers);
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
                    
                    if (response != null && response.getBody() != null) {
                        log.debug("Raw Rally API response: {}", response.getBody());
                        return response;
                    } else {
                        log.warn("Received null response or body from Rally API");
                        return null;
                    }
                } else {
                    log.error("No access token found for connection ID: {}", projectConfig.getProjectToolConfig().getConnectionId());
                    return null;
                }
            } else {
                log.error("Invalid project tool config or connection ID");
                return null;
            }
        } catch (Exception e) {
            log.error("Error making Rally API request to URL: " + url, e);
            throw e;
        }
    }

    public String getWorkspacePath() {
        return WORKSPACE_PATH;
    }

    public String getProjectPath() {
        return PROJECT_PATH;
    }

    public String getTypedefinitionPath() {
        return TYPEDEFINITION_PATH;
    }

    public String getAllowedValuesPath() {
        return ALLOWED_VALUES_PATH;
    }
}
