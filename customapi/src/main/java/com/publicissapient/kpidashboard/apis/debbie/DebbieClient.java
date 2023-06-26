package com.publicissapient.kpidashboard.apis.debbie;

import com.google.gson.Gson;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.debbie.model.DebbieConfig;
import com.publicissapient.kpidashboard.apis.debbie.model.RepoActivity;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class DebbieClient {

    private RestTemplate restTemplate;
    private static final String DEBBIE_ENROLL_URL = "/beta/repositories/";
    private static final String DEBBIE_TRIGGER_SCAN_URL = "/metric/%s/trigger-scan";

    private static final String REPO_ACTIVITY_URL = "/metric/{project_code}/repo-activity/";
    private static final String X_API_KEY = "X-API-Key";
    private HttpHeaders httpHeaders;

    @Autowired
    private CustomApiConfig customApiConfig;


    public DebbieClient() {
        this.restTemplate = new RestTemplate();
    }

    public int enrollProjectCall(DebbieConfig debbieConfig) {
        setHttpHeaders();
        Gson gson = new Gson();
        String payload = gson.toJson(debbieConfig);
        HttpEntity<String> entity = new HttpEntity<>(payload, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(customApiConfig.getDebbieURL() + DEBBIE_ENROLL_URL,
                HttpMethod.POST, entity, String.class);
        return response.getStatusCode().value();
    }

	public int triggerScanCall(String projectKey) {
        setHttpHeaders();
		String triggerScanUrl = String.format(DEBBIE_TRIGGER_SCAN_URL, projectKey);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		ResponseEntity<String> response = restTemplate.exchange(customApiConfig.getDebbieURL() + triggerScanUrl,
				HttpMethod.GET, entity, String.class);
		return response.getStatusCode().value();

	}

//    public RepoActivity repoActivityCall(String projectKey) {
//        setHttpHeaders();
//        String repoActivityUrl = String.format(REPO_ACTIVITY_URL, projectKey);
//        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
//        ResponseEntity<List<RepoActivity>> response = restTemplate.getForEntity(customApiConfig.getDebbieURL() + repoActivityUrl, entity, RepoActivity.class);
//    }

    public void setHttpHeaders() {
        httpHeaders.add(X_API_KEY, customApiConfig.getDebbieAPIKey());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

}
