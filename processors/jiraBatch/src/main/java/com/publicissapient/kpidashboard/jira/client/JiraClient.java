package com.publicissapient.kpidashboard.jira.client;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.config.JiraOAuthProperties;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.factory.ProcessorAsynchJiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.model.JiraInfo;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JiraClient {

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	private ConnectionRepository connectionRepository;

	@Autowired
	private ToolCredentialProvider toolCredentialProvider;

	@Autowired
	private ProjectToolConfigRepository toolRepository;

	@Autowired
	private JiraOAuthProperties jiraOAuthProperties;

	@Autowired
	private JiraOAuthClient jiraOAuthClient;

	@Autowired
	private JiraCommonService jiraCommonService;

	private ProcessorJiraRestClient client;

	KerberosClient krb5Client;

	public ProcessorJiraRestClient getClient(Map.Entry<String, ProjectConfFieldMapping> entry) {
		ProjectConfFieldMapping projectConfig = entry.getValue();
		List<ProjectToolConfig> jiraDetails = toolRepository.findByToolNameAndBasicProjectConfigId(
				ProcessorConstants.JIRA, projectConfig.getBasicProjectConfigId());
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(jiraDetails)
				&& jiraDetails.get(0).getConnectionId() != null) {
			Optional<Connection> jiraConn = connectionRepository.findById(jiraDetails.get(0).getConnectionId());
			if (jiraConn.isPresent() && projectConfig.getJira().getConnection().isPresent()) {
				projectConfig.setProjectToolConfig(jiraDetails.get(0));
				boolean isOauth = jiraConn.get().getIsOAuth();
				Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
				if (connectionOptional.isPresent()) {
					Connection conn = connectionOptional.get();
					client = getProcessorRestClient(entry, isOauth, conn);
				}
			}
		}
		return client;
	}

	private ProcessorJiraRestClient getProcessorRestClient(Map.Entry<String, ProjectConfFieldMapping> entry,
			boolean isOauth, Connection conn) {
		if (conn.isJaasKrbAuth()) {
			KerberosClient krb5Client = new KerberosClient(conn.getJaasConfigFilePath(), conn.getKrb5ConfigFilePath(),
					conn.getJaasUser(), conn.getSamlEndPoint(), conn.getBaseUrl());
			return getSpnegoSamlClient(krb5Client);
		} else {
			return getProcessorJiraRestClient(entry, isOauth, conn);
		}
	}

	public ProcessorJiraRestClient getSpnegoSamlClient(KerberosClient kerberosClient) {
		ProcessorJiraRestClient client = null;
		kerberosClient.login(jiraProcessorConfig.getSamlTokenStartString(), jiraProcessorConfig.getSamlTokenEndString(),
				jiraProcessorConfig.getSamlUrlStartString(), jiraProcessorConfig.getSamlUrlEndString());
		client = new ProcessorAsynchJiraRestClientFactory().createWithAuthenticationCookies(
				URI.create(kerberosClient.getJiraHost()), kerberosClient.getCookies(), jiraProcessorConfig);
		return client;
	}

	private ProcessorJiraRestClient getProcessorJiraRestClient(Map.Entry<String, ProjectConfFieldMapping> entry,
			boolean isOauth, Connection conn) {
		ProcessorJiraRestClient client;

		String username = "";
		String password = "";
		if (conn.isVault()) {
			ToolCredential toolCredential = toolCredentialProvider.findCredential(conn.getUsername());
			if (toolCredential != null) {
				username = toolCredential.getUsername();
				password = toolCredential.getPassword();
			}

		} else {
			username = conn.getUsername();
			password = jiraCommonService.decryptJiraPassword(conn.getPassword());
		}

		if (isOauth) {
			// Sets Jira OAuth properties
			jiraOAuthProperties.setJiraBaseURL(conn.getBaseUrl());
			jiraOAuthProperties.setConsumerKey(conn.getConsumerKey());
			jiraOAuthProperties.setPrivateKey(jiraCommonService.decryptJiraPassword(conn.getPrivateKey()));

			// Generate and save accessToken
			saveAccessToken(entry);
			jiraOAuthProperties.setAccessToken(conn.getAccessToken());

			client = getJiraOAuthClient(JiraInfo.builder().jiraConfigBaseUrl(conn.getBaseUrl()).username(username)
					.password(password).jiraConfigAccessToken(conn.getAccessToken()).jiraConfigProxyUrl(null)
					.jiraConfigProxyPort(null).build());

		} else {

			client = getJiraClient(JiraInfo.builder().jiraConfigBaseUrl(conn.getBaseUrl()).username(username)
					.password(password).jiraConfigProxyUrl(null).jiraConfigProxyPort(null)
					.bearerToken(conn.isBearerToken()).build());

		}
		return client;
	}

	public ProcessorJiraRestClient getJiraClient(JiraInfo jiraInfo) {
		String username = jiraInfo.getUsername();
		String password = jiraInfo.getPassword();
		String jiraConfigBaseUrl = jiraInfo.getJiraConfigBaseUrl();
		String jiraConfigProxyUrl = jiraInfo.getJiraConfigProxyUrl();
		String jiraConfigProxyPort = jiraInfo.getJiraConfigProxyPort();
		ProcessorJiraRestClient client = null;
		String proxyUri = null;
		String proxyPort = null;

		URI jiraUri = null;

		try {
			if (jiraConfigProxyUrl == null || jiraConfigProxyUrl.isEmpty() || (jiraConfigProxyPort == null)) {
				jiraUri = new URI(jiraConfigBaseUrl);
			} else {
				proxyUri = jiraConfigProxyUrl;
				proxyPort = jiraConfigProxyPort;

				jiraUri = this.createJiraConnection(jiraConfigBaseUrl, proxyUri + ":" + proxyPort, username, password);
			}

			InetAddress.getByName(jiraUri.getHost());// NOSONAR
			client = new ProcessorAsynchJiraRestClientFactory().createWithBasicHttpAuthentication(jiraUri, username,
					password, jiraProcessorConfig);

		} catch (UnknownHostException | URISyntaxException e) {
			log.error("The Jira host name is invalid. Further jira collection cannot proceed.");
			log.debug("Exception", e);
		}

		return client;
	}

	public ProcessorJiraRestClient getJiraOAuthClient(JiraInfo jiraInfo) {
		String username = jiraInfo.getUsername();
		String password = jiraInfo.getPassword();
		String jiraConfigBaseUrl = jiraInfo.getJiraConfigBaseUrl();
		String jiraConfigProxyUrl = jiraInfo.getJiraConfigProxyUrl();
		String jiraConfigProxyPort = jiraInfo.getJiraConfigProxyPort();
		ProcessorJiraRestClient client = null;
		String proxyUri = null;
		String proxyPort = null;

		URI jiraUri = null;

		try {
			if (jiraConfigProxyUrl == null || jiraConfigProxyUrl.isEmpty() || (jiraConfigProxyPort == null)) {
				jiraUri = new URI(jiraConfigBaseUrl);
			} else {
				proxyUri = jiraConfigProxyUrl;
				proxyPort = jiraConfigProxyPort;

				jiraUri = this.createJiraConnection(jiraConfigBaseUrl, proxyUri + ":" + proxyPort, username, password);
			}

			InetAddress.getByName(jiraUri.getHost());// NOSONAR
			client = new ProcessorAsynchJiraRestClientFactory().create(jiraUri, jiraOAuthClient, jiraProcessorConfig);

		} catch (UnknownHostException | URISyntaxException e) {
			log.error("The Jira host name is invalid. Further jira collection cannot proceed.");

			log.debug("Exception", e);
		}

		return client;
	}

	private URI createJiraConnection(String jiraBaseUri, String fullProxyUrl, String username, String password) {
		final String uname = username;
		final String pword = password;
		Proxy proxy = null;
		URLConnection connection = null;
		try {
			if (StringUtils.isNotEmpty(jiraBaseUri)) {
				URL baseUrl = new URL(jiraBaseUri);
				if (StringUtils.isNotEmpty(fullProxyUrl)) {
					URL proxyUrl = new URL(fullProxyUrl);
					URI proxyUri = new URI(proxyUrl.getProtocol(), proxyUrl.getUserInfo(), proxyUrl.getHost(),
							proxyUrl.getPort(), proxyUrl.getPath(), proxyUrl.getQuery(), null);
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort()));
					connection = baseUrl.openConnection(proxy);

					if (!StringUtils.isEmpty(username) && (!StringUtils.isEmpty(password))) {
						String creds = uname + ":" + pword;
						Authenticator.setDefault(new Authenticator() {
							@Override
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(uname, pword.toCharArray());
							}
						});
						connection.setRequestProperty("Proxy-Authorization",
								"Basic " + Base64.encodeBase64String((creds).getBytes()));
					}
				} else {
					connection = baseUrl.openConnection();
				}
			} else {
				log.error(
						"The response from Jira was blank or non existant - please check your property configurations");
				return null;
			}

			return connection.getURL().toURI();

		} catch (URISyntaxException | IOException e) {
			try {
				log.error(
						"There was a problem parsing or reading the proxy configuration settings during openning a Jira connection. Defaulting to a non-proxy URI.");
				return new URI(jiraBaseUri);
			} catch (URISyntaxException e1) {
				log.error("Correction:  The Jira connection base URI cannot be read!");
				return null;
			}
		}
	}

	public void saveAccessToken(Map.Entry<String, ProjectConfFieldMapping> entry) {
		Optional<Connection> connectionOptional = entry.getValue().getJira().getConnection();
		if (connectionOptional.isPresent()) {
			Optional<String> checkNull = Optional.ofNullable(connectionOptional.get().getAccessToken());
			if (!checkNull.isPresent() || checkNull.get().isEmpty()) {

				JiraToolConfig jiraToolConfig = entry.getValue().getJira();
				generateAndSaveAccessToken(jiraToolConfig);
			}
		}
	}

	/**
	 * Generate and save accessToken
	 *
	 * @param jiraToolConfig
	 */
	private void generateAndSaveAccessToken(JiraToolConfig jiraToolConfig) {

		Optional<Connection> connectionOptional = jiraToolConfig.getConnection();
		if (connectionOptional.isPresent()) {
			String username = connectionOptional.get().getUsername();
			String plainTextPassword = jiraCommonService.decryptJiraPassword(connectionOptional.get().getPassword());

			String accessToken;
			try {
				accessToken = jiraOAuthClient.getAccessToken(username, plainTextPassword);
				connectionOptional.get().setAccessToken(accessToken);
				connectionRepository.save(connectionOptional.get());
			} catch (FailingHttpStatusCodeException e) {
				log.error("HTTP Status code error while generating accessToken", e);
			} catch (MalformedURLException e) {
				log.error("Malformed URL error while generating accessToken", e);
			} catch (IOException e) {
				log.error("Error while generating accessToken", e);
			}
		}
	}

}
