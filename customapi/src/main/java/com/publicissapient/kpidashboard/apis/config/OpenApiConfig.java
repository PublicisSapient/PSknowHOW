/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * @author shunaray
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().info(new Info().title("PSKnowHOW API Documentation").version("1.0.0").description(
				"PSKnowHOW is a measurement framework that delivers an intuitive, visual dashboard to track key performance indicators (KPIs) across entire organizations transformation programs. It empowers teams with the knowledge of HOW work is progressing, areas of health, as well as achievement gaps and areas for improvement.")
				.license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
				.summary("PSKnowHOW API Documentation Summary"))
				.components(
						new Components()
								.addSecuritySchemes("cookieAuth",
										new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE)
												.name("authCookie"))
								.addSecuritySchemes("apiKeyAuth",
										new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
												.name("x-api-key")))
				.addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
				.addSecurityItem(new SecurityRequirement().addList("apiKeyAuth"))
				.externalDocs(new ExternalDocumentation().description("Find out more about PSKnowHOW")
						.url("https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/1212417/About+PSknowHOW"));
	}
}
