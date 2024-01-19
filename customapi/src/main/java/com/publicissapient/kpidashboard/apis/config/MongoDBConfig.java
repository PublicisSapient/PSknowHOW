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

    @Value("${mongodb.connection.atlas}")
    private boolean useAtlasDB;

    @Value("${spring.data.mongodb.uri}")
    private String mongoDBUri;

    @Value("${spring.data.mongodb.atlas.uri}")
    private String atlasUri;

    public String getMongoDBUri() {
        return useAtlasDB ? atlasUri : mongoDBUri;
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(getMongoDBUri());
    }
}


