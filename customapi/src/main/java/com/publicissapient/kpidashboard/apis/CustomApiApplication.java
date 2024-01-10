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

package com.publicissapient.kpidashboard.apis;


import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.util.DefaultLogoInsertor;

import io.mongock.runner.springboot.EnableMongock;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * CustomApiApplication class is the entry point of all Sprint boot application.
 * <p>
 * CustomApiApplication configuration and bootstrap
 * 
 * @author girpatha
 */
@SpringBootApplication
@EnableCaching
@EnableMongock
@EnableMongoRepositories(basePackages = { "com.publicissapient.**.repository" })
@ComponentScan(basePackages = { "com.publicissapient.kpidashboard" })
public class CustomApiApplication extends SpringBootServletInitializer {

	/**
	 * {@inheritDoc}
	 */

	/**
	 * This method run CustomApiApplication.run() because this method starts whole
	 * Spring Framework. This method integrates main() with Spring Boot.
	 *
	 * @param args
	 */
	@SuppressWarnings("PMD.CloseResource")
	public static void main(String[] args) {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
		ConfigurableApplicationContext configurableApplicationContext = new CustomApiApplication()
				.configure(new SpringApplicationBuilder(CustomApiApplication.class)).run(args);
		DefaultLogoInsertor imageInsertor = configurableApplicationContext.getBean(DefaultLogoInsertor.class);
		imageInsertor.insertDefaultImage();
	}

	/**
	 * This method provide the configuration to integrate Swagger 2 into an existing
	 * Sprint Boot CustomApiApplication
	 * 
	 * @return Bean of Docket class
	 */
	@Bean
	public Docket documentation() {
		return new Docket(DocumentationType.SWAGGER_2).enable(true).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build().pathMapping("/").apiInfo(metadata());
	}

	/**
	 * This method provide the informational part of documentation for swagger:
	 * title, version, license, description, etc.
	 * 
	 * @return An instance of AppInfor class
	 */
	private ApiInfo metadata() {
		return new ApiInfoBuilder().title("KnowHOW API").description("API Documentation for KnowHOW").version("2.0")
				.build();
	}

	/**
	 * This method provide the instance of LocalValidatorFactoryBean which supports
	 * Bean Validation 1.0 and 1.1
	 * 
	 * @return Bean of Validator class
	 */
	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

	/**
	 * This method define a MethodValidationPostProcessor bean so that the
	 * annotations like @Validated could work and method level validation can be
	 * done
	 * 
	 * @return Bean of MethodValidationPostProcessor
	 */
	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor() {
		MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
		methodValidationPostProcessor.setValidator(validator());
		return methodValidationPostProcessor;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
