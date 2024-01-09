package com.publicissapient.kpidashboard.jira.cache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class JiraProcessorCacheEvictorTest {

    @InjectMocks
    private JiraProcessorCacheEvictor jiraProcessorCacheEvictor;
    @Mock
    private JiraProcessorConfig jiraProcessorConfig;
    @Mock
    private RestTemplate restTemplate;

    @Test
    public void testEvictCache_SuccessfulEviction() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("Success", HttpStatus.OK);

        when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://example.com");

        boolean cleaned = jiraProcessorCacheEvictor.evictCache("cacheEndPoint", "cacheName");
        assertTrue(!cleaned);
    }

    @Test
    public void testEvictCache_UnsuccessfulEviction() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);

        when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://example.com");

        boolean cleaned = jiraProcessorCacheEvictor.evictCache("cacheEndPoint", "cacheName");

        assertFalse(cleaned);
    }
}

