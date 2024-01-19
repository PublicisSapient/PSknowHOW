package com.publicissapient.kpidashboard.apis.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@Configuration
@PropertySource({"classpath:application.properties"})
public class MongoDBConfig {

    @Value("${mongodb.connection.local}")
    private boolean useLocalMongoDB;

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.atlas.host}")
    private String atlasHost;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.username}")
    private String user;

    @Value("${spring.data.mongodb.password}")
    private String password;

    public String getDbHOST() {
        return useLocalMongoDB ? host : atlasHost;
    }

    public MongoClient mongoClient() {
        MongoCredential credential = MongoCredential.createCredential(user, database, password.toCharArray());
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(new ServerAddress(getDbHOST(), port))))
                .credential(credential)
                .build();
        return MongoClients.create(settings);
    }
}


