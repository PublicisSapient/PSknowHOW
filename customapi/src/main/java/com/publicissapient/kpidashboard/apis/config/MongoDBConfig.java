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

    @Value("${spring.data.mongodb.uri}")
    private String mongoDBUri;

    @Value("${spring.data.mongodb.uri}")
    private String atlasUri;


    public String getMongoDBUri() {
        return useLocalMongoDB ? mongoDBUri : atlasUri;
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(getMongoDBUri());
    }

    public MongoClient mongoClient() {
        MongoCredential credential = MongoCredential.createCredential(user, database, password.toCharArray());
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(new ServerAddress(host, port))))
                .credential(credential)
                .build();
        return MongoClients.create(settings);
    }
}


