package com.publicissapient.kpidashboard.apis.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

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
	 *            aesEncryptionService
	 * @param customApiConfig
	 *            customApiConfig
	 * @return mongo client
	 */
	@Bean
	public MongoClient mongoClient(AesEncryptionService aesEncryptionService, CustomApiConfig customApiConfig) {
		password = aesEncryptionService.decrypt(password, customApiConfig.getAesEncryptionKey());
		MongoCredential credential = MongoCredential.createCredential(user, database, password.toCharArray());
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyToClusterSettings(
						builder -> builder.hosts(Collections.singletonList(new ServerAddress(host, port))))
				.credential(credential).build();
		return MongoClients.create(settings);
	}
}
