package com.publicissapient.kpidashboard.jira.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:application.properties"})
public class MongoDBConfig {

    @Value("${mongodb.connection.local}")
    private boolean useLocalMongoDB;

    @Value("${spring.data.mongodb.host}")
    private String mongoDBHost;

    @Value("${spring.data.mongodb.port}")
    private int mongoDBPort;

    @Value("${spring.data.mongodb.database}")
    private String mongoDBDatabase;

    @Value("${spring.data.mongodb.username}")
    private String mongoDBUsername;

    @Value("${spring.data.mongodb.password}")
    private String mongoDBPassword;

    @Value("${spring.data.mongodb.uri}")
    private String mongoDBAtlasUri;


    public String getMongoDBUri() {
        return useLocalMongoDB ? buildLocalMongoDBUri() : mongoDBAtlasUri;
    }

    private String buildLocalMongoDBUri() {
        return "mongodb://" + mongoDBHost + ":" + mongoDBPort + "/" + mongoDBDatabase;
    }
}

