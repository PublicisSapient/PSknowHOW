package com.publicissapient.kpidashboard.azurepipeline.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.username}")
    private String user;

    @Value("${spring.data.mongodb.password}")
    private String password;

    /**
     * This method create mongoClient
     *
     * @param aesEncryptionService
     * 		aesEncryptionService
     * @param azurePipelineConfig
     * 		azurePipelineConfig
     * @return mongo client
     */
    @Bean
    public MongoClient mongoClient(AesEncryptionService aesEncryptionService, AzurePipelineConfig azurePipelineConfig) {
        password = aesEncryptionService.decrypt(password, azurePipelineConfig.getAesEncryptionKey());
        MongoCredential credential = MongoCredential.createCredential(user, database, password.toCharArray());
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(new ServerAddress(host, port))))
                .credential(credential)
                .build();
        return MongoClients.create(settings);
    }
}
