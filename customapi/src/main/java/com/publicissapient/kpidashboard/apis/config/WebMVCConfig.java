/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.config;

import java.util.List;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.apis.mapper.CustomObjectMapper;

/**
 * An extension of {@link WebMvcConfigurer} to provide project specific
 * web mvc configuration.
 * 
 * @author anisingh4
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.publicissapient.kpidashboard.apis")
public class WebMVCConfig implements WebMvcConfigurer {
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable("api");
	}

	// TODO
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		MappingJackson2HttpMessageConverter jackson = new MappingJackson2HttpMessageConverter();

		jackson.setObjectMapper(new CustomObjectMapper());
		jackson.getObjectMapper().disable(SerializationFeature.WRITE_NULL_MAP_VALUES)
				.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
				.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		jackson.getObjectMapper().registerModule(new JavaTimeModule());

		converters.add(jackson);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
		resolver.setMaxPageSize(Integer.MAX_VALUE);
		argumentResolvers.add(resolver);
	}

	/*
	 * Added for Swagger
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String[] staticResourceMappingPath = { "classpath:/static/" };

		registry.addResourceHandler("/**").addResourceLocations(staticResourceMappingPath);
	}

}