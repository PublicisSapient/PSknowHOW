/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2020 Sapient Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.jiratest.adapter.impl.async.factory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;
import com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import com.publicissapient.kpidashboard.jiratest.config.JiraTestProcessorConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessorAsynchHttpClientFactory extends AsynchronousHttpClientFactory {

	/**
	 * Creates Speedy Client
	 *
	 * @param serverUri
	 *            Jira server URI
	 * @param authenticationHandler
	 *            authentication handler instance
	 * @param jiraTestProcessorConfig
	 *            Jira test processor configuration
	 * @return DisposableHttpClient
	 */
	@SuppressWarnings("unchecked")
	public DisposableHttpClient createProcessorClient(final URI serverUri,
			final AuthenticationHandler authenticationHandler, JiraTestProcessorConfig jiraTestProcessorConfig) {
		final HttpClientOptions options = new HttpClientOptions();
		options.setSocketTimeout(jiraTestProcessorConfig.getSocketTimeOut(), TimeUnit.SECONDS);
		final DefaultHttpClientFactory defaultHttpClientFactory = new DefaultHttpClientFactory(new NoOpEventPublisher(),
				new RestClientApplicationProperties(serverUri), new ThreadLocalContextManager() {
					@Override
					public Object getThreadLocalContext() {
						return null;
					}

					@Override
					public void setThreadLocalContext(Object context) {
						// set context
						// default implementation ignored
					}

					@Override
					public void clearThreadLocalContext() {
						// clear context
						// default implementation ignored
					}
				});

		final HttpClient httpClient = defaultHttpClientFactory.create(options);

		return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
			@Override
			public void destroy() throws Exception {
				defaultHttpClientFactory.dispose(httpClient);
			}
		};
	}

	private static class NoOpEventPublisher implements EventPublisher {
		@Override
		public void publish(Object obj) {
			// publish event
		}

		@Override
		public void register(Object obj) {
			// Register event
		}

		@Override
		public void unregister(Object obj) {
			// unregister event
		}

		@Override
		public void unregisterAll() {
			// unregister all event
		}
	}

	/**
	 * These properties are used to present JRJC as a User-Agent during http
	 * requests.
	 */
	@SuppressWarnings("deprecation")
	private static class RestClientApplicationProperties implements ApplicationProperties {

		private final String baseUrl;

		private RestClientApplicationProperties(URI jiraURI) {
			this.baseUrl = jiraURI.getPath();
		}

		@Override
		public String getBaseUrl() {
			return baseUrl;
		}

		/**
		 * We'll always have an absolute URL as a client.
		 */
		@Nonnull
		@Override
		public String getBaseUrl(UrlMode urlMode) {
			return baseUrl;
		}

		@Nonnull
		@Override
		public String getDisplayName() {
			return "Atlassian JIRA Rest Java Client";
		}

		@Nonnull
		@Override
		public String getPlatformId() {
			return ApplicationProperties.PLATFORM_JIRA;
		}

		@Nonnull
		@Override
		public String getVersion() {
			return MavenUtils.getVersion("com.atlassian.jira", "jira-rest-java-client-core");
		}

		@Nonnull
		@Override
		public Date getBuildDate() {
			throw new UnsupportedOperationException();
		}

		@Nonnull
		@Override
		public String getBuildNumber() {
			return String.valueOf(0);
		}

		@Override
		public File getHomeDirectory() {
			return new File(".");
		}

		@Nonnull
		@Override
		public Optional<Path> getLocalHomeDirectory() {
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<Path> getSharedHomeDirectory() {
			return Optional.empty();
		}

		@Override
		public String getPropertyValue(final String s) {
			throw new UnsupportedOperationException("Not implemented");
		}

		@Nonnull
		@Override
		public String getApplicationFileEncoding() {
			return null;
		}
	}

	private static final class MavenUtils {

		private static final String UNKNOWN_VERSION = "unknown";

		static String getVersion(String groupId, String artifactId) {
			final Properties props = new Properties();
			InputStream resourceAsStream = null;
			try {
				resourceAsStream = MavenUtils.class.getResourceAsStream(
						String.format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId));
				props.load(resourceAsStream);
				return props.getProperty("version", UNKNOWN_VERSION);
			} catch (Exception e) {
				log.debug("Could not find version for maven artifact {}:{}", groupId, artifactId);
				log.debug("Got the following exception", e);
				return UNKNOWN_VERSION;
			} finally {
				if (resourceAsStream != null) {
					try {
						resourceAsStream.close();
					} catch (IOException ioe) {
						// ignore
					}
				}
			}
		}
	}

}
