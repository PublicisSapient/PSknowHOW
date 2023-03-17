package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.Optional;

@Slf4j
@Component
public class JiraCommonService {

    //function common in all any file except kanban and scrum goes here

    @Autowired
    private JiraProcessorConfig jiraProcessorConfig;

    @Autowired
    private ToolCredentialProvider toolCredentialProvider;

    @Autowired
    private AesEncryptionService aesEncryptionService;


    public String getDataFromServer(ProjectConfFieldMapping projectConfig, HttpURLConnection connection)
            throws IOException {
        HttpURLConnection request = connection;
        Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();

        String username = null;
        String password = null;

        if(connectionOptional.isPresent()) {
            Connection conn = connectionOptional.get();
            if (conn.isVault()) {
                ToolCredential toolCredential = toolCredentialProvider.findCredential(conn.getUsername());
                if (toolCredential != null) {
                    username = toolCredential.getUsername();
                    password = toolCredential.getPassword();
                }

            } else {
                username = connectionOptional.map(Connection::getUsername).orElse(null);
                password = decryptJiraPassword(connectionOptional.map(Connection::getPassword).orElse(null));
            }
        }
        request.setRequestProperty("Authorization", "Basic " + encodeCredentialsToBase64(username, password)); // NOSONAR
        request.connect();
        StringBuilder sb = new StringBuilder();
        try (InputStream in = (InputStream) request.getContent();
             BufferedReader inReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));) {
            int cp;
            while ((cp = inReader.read()) != -1) {
                sb.append((char) cp);
            }
        } catch (IOException ie) {
            log.error("Read exception when connecting to server {}", ie);
        }
        return sb.toString();
    }

    public String decryptJiraPassword(String encryptedPassword) {
        return aesEncryptionService.decrypt(encryptedPassword, jiraProcessorConfig.getAesEncryptionKey());
    }

    public String encodeCredentialsToBase64(String username, String password) {
        String cred = username + ":" + password;
        return Base64.getEncoder().encodeToString(cred.getBytes());
    }

}
