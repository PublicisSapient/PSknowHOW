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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.publicissapient.kpidashboard.apis.filters.apitoken.ApiTokenAuthenticationProvider;
import com.publicissapient.kpidashboard.apis.filters.apitoken.ApiTokenRequestFilter;
import com.publicissapient.kpidashboard.apis.filters.standard.AuthenticationResultHandler;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(1)
public class ApiTokenSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthenticationResultHandler standardAuthenticationResultHandler;

	@Autowired
	private ApiTokenAuthenticationProvider apiTokenAuthenticationProvider;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.headers().cacheControl();
		http.csrf().disable().authorizeRequests().antMatchers("/userlogout/**").permitAll().antMatchers("/user/**")
				.permitAll().antMatchers("/user-approvals/pending").permitAll().antMatchers("/update-userApproval/**")
				.permitAll().antMatchers("/deleteUser/**").permitAll().antMatchers("/changePassword").authenticated()
				.and().httpBasic().and().csrf().disable().headers().and()
				.addFilterBefore(apiTokenRequestFilter(), UsernamePasswordAuthenticationFilter.class);
	}

	@Bean
	protected ApiTokenRequestFilter apiTokenRequestFilter() throws Exception {
		return new ApiTokenRequestFilter("/**", authenticationManager());
	}

	@Bean
	protected CorsFilter corsFilterKnowHOW() throws Exception {
		return new CorsFilter();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(apiTokenAuthenticationProvider);
	}
}
