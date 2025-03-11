package com.publicissapient.kpidashboard.rally.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.RallyToolConfig;

@ExtendWith(MockitoExtension.class)
public class RallyCommonServiceTest {

    @InjectMocks
    private RallyCommonService rallyCommonService;

    @Mock
    private RallyProcessorConfig rallyProcessorConfig;

    @Mock
    private ToolCredentialProvider toolCredentialProvider;

    @Mock
    private AesEncryptionService aesEncryptionService;

    @Mock
    private ProcessorToolConnectionService processorToolConnectionService;

    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KerberosClient krb5Client;

    private ProjectConfFieldMapping projectConfig;
    private Connection connection;
    private RallyToolConfig rallyToolConfig;

    @BeforeEach
    public void setup() {
        projectConfig = new ProjectConfFieldMapping();
        projectConfig.setBasicProjectConfigId(new ObjectId());
        ProjectBasicConfig basicConfig = new ProjectBasicConfig();
        basicConfig.setId(new ObjectId());
        projectConfig.setProjectBasicConfig(basicConfig);

        connection = new Connection();
        connection.setOffline(false);
        connection.setUsername("testuser");
        connection.setPassword("encryptedPassword");

        rallyToolConfig = new RallyToolConfig();
    }

    @Test
    public void testGetDataFromClientWithBasicAuth() throws Exception {
        URL testUrl = new URL("https://rally1.rallydev.com/test");
        when(aesEncryptionService.decrypt(anyString(), any())).thenReturn("decryptedPassword");

        String result = rallyCommonService.getDataFromClient(projectConfig, testUrl, krb5Client);
        assertNotNull(result);
    }

    @Test
    public void testGetDataFromClientWithVaultCredentials() throws Exception {
        URL testUrl = new URL("https://rally1.rallydev.com/test");
        connection.setVault(true);
        
        ToolCredential toolCredential = new ToolCredential();
        toolCredential.setUsername("vaultUser");
        toolCredential.setPassword("vaultPassword");
        
        when(toolCredentialProvider.findCredential(anyString())).thenReturn(toolCredential);

        String result = rallyCommonService.getDataFromClient(projectConfig, testUrl, krb5Client);
        assertNotNull(result);
    }

    @Test
    public void testGetDataFromClientWithBearerToken() throws Exception {
        URL testUrl = new URL("https://rally1.rallydev.com/test");
        connection.setBearerToken(true);
        connection.setPatOAuthToken("encryptedToken");
        
        when(aesEncryptionService.decrypt(anyString(), any())).thenReturn("decryptedToken");

        String result = rallyCommonService.getDataFromClient(projectConfig, testUrl, krb5Client);
        assertNotNull(result);
    }

    @Test
    public void testGetDataFromClientWithSpnego() throws Exception {
        URL testUrl = new URL("https://rally1.rallydev.com/test");
        connection.setJaasKrbAuth(true);
        
        when(krb5Client.getResponse(any())).thenReturn("spnegoResponse");

        String result = rallyCommonService.getDataFromClient(projectConfig, testUrl, krb5Client);
        assertEquals("spnegoResponse", result);
    }

    @Test
    public void testGetDataFromClientWithInvalidCredentials() throws Exception {
        URL testUrl = new URL("https://rally1.rallydev.com/test");
        connection.setUsername("invalid");
        connection.setPassword("invalid");

        when(aesEncryptionService.decrypt(anyString(), any())).thenReturn("invalid");

        assertThrows(IOException.class, () -> {
            rallyCommonService.getDataFromClient(projectConfig, testUrl, krb5Client);
        });
    }

    @Test
    public void testGetDataFromClientWithOfflineConnection() throws Exception {
        URL testUrl = new URL("https://rally1.rallydev.com/test");
        connection.setOffline(true);

        assertThrows(IOException.class, () -> {
            rallyCommonService.getDataFromClient(projectConfig, testUrl, krb5Client);
        });
    }

    @Test
    public void testGetDataFromClientWithMalformedUrl() {
        assertThrows(IOException.class, () -> {
            URL testUrl = new URL("invalid://url");
            rallyCommonService.getDataFromClient(projectConfig, testUrl, krb5Client);
        });
    }

    @Test
    public void testGetDataFromServerWithNoConnection() throws Exception {
        URL testUrl = new URL("https://rally1.rallydev.com/test");
        String result = rallyCommonService.getDataFromServer(testUrl, Optional.empty(), new ObjectId());
        assertNotNull(result);
    }

    @Test
    public void testProcessClientError() throws Exception {
        URL testUrl = new URL("https://rally1.rallydev.com/test");
        connection.setOffline(false);

        assertThrows(IOException.class, () -> {
            rallyCommonService.getDataFromServer(testUrl, Optional.of(connection), new ObjectId());
        });
    }
}
