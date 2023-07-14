/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
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

package com.publicissapient.kpidashboard.apis.config;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;

/**
 * Spring context configuration for Testing purposes
 */
@Configuration
@PropertySource("classpath:/application.properties")
public class CustomTestConfig {

	@Bean
	public CustomApiConfig customApiConfig() {
		return new CustomApiConfig();
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		return Mockito.mock(AuthenticationManager.class);
	}

	@Bean
	public SpringTemplateEngine springTemplateEngine() {
		return Mockito.mock(SpringTemplateEngine.class);
	}

	@Bean
	public SpringResourceTemplateResolver springResourceTemplateResolver() {
		return Mockito.mock(SpringResourceTemplateResolver.class);
	}

	@Bean
	public CacheManager cacheManager() {
		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
		List<String> caches = new ArrayList<>();
		caches.add(CommonConstant.CACHE_ACCOUNT_HIERARCHY);
		caches.add(CommonConstant.CACHE_ACCOUNT_HIERARCHY_KANBAN);
		cacheManager.setCacheNames(caches);
		return cacheManager;
	}

}