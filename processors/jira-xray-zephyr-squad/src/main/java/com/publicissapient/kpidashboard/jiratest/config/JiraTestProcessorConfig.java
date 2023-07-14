package com.publicissapient.kpidashboard.jiratest.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jira.test")
public class JiraTestProcessorConfig {

	private String cron;
	private int pageSize;
	private String startDate;
	private long minsToReduce;
	private String customApiBaseUrl;
	private Integer socketTimeOut;
	private int threadPoolSize;
	@Value("${aesEncryptionKey}")
	private String aesEncryptionKey;
	private String jiraCloudGetUserApi;
	private String jiraServerGetUserApi;
	private List<String> excludeLinks;
	private String jiraDirectTicketLinkKey;
	private String jiraCloudDirectTicketLinkKey;
	private boolean considerStartDate;

}
