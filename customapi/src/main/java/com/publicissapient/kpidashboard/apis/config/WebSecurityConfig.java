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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.AuthenticationResultHandler;
import com.publicissapient.kpidashboard.apis.auth.CustomAuthenticationFailureHandler;
import com.publicissapient.kpidashboard.apis.auth.service.AuthTypesConfigService;
import com.publicissapient.kpidashboard.apis.auth.standard.StandardAuthenticationManager;
import com.publicissapient.kpidashboard.apis.auth.standard.StandardLoginRequestFilter;
import com.publicissapient.kpidashboard.apis.auth.token.JwtAuthenticationFilter;
import com.publicissapient.kpidashboard.apis.errors.CustomAuthenticationEntryPoint;
import com.publicissapient.kpidashboard.common.constant.AuthType;

import lombok.AllArgsConstructor;

/**
 * Extension of {WebSecurityConfigurerAdapter} to provide configuration
 * for web security.
 *
 * @author anisingh4
 *
 * @author pawkandp
 * Removed  the depricate WebSecurityConfigurerAdapter with new spring version 6+
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig implements WebMvcConfigurer {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private AuthenticationResultHandler authenticationResultHandler;

    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    private AuthenticationProvider standardAuthenticationProvider;

    private AuthProperties authProperties;

    private CustomApiConfig customApiConfig;

    private AuthTypesConfigService authTypesConfigService;

    private StandardAuthenticationManager authenticationManager;

    public static Properties getProps() throws IOException {
        Properties prop = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("crowd.properties")) {
            prop.load(in);
        }
        return prop;
    }

    /**
     * Added below fixes for security scan: - commented the headers in the response
     * - added CorsFilter in filter chain for endpoints mentioned in the method
     *
     * @param http - reference to HttpSecurity
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configure AuthenticationManagerBuilder
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        setAuthenticationProvider(authenticationManagerBuilder);
        http.headers(headers -> headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable)
				.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(customApiConfig.isIncludeSubDomains())
						.maxAgeInSeconds(customApiConfig.getMaxAgeInSeconds())));
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authz -> authz
                        .requestMatchers("/appinfo").permitAll().requestMatchers("/registerUser")
                        .permitAll().requestMatchers("/changePassword").permitAll().requestMatchers("/login/captcha").permitAll()
                        .requestMatchers("/login/captchavalidate").permitAll().requestMatchers("/login**").permitAll()
                        .requestMatchers("/error").permitAll().requestMatchers("/authenticationProviders").permitAll()
                        .requestMatchers("/auth-types-status").permitAll().requestMatchers("/pushData/*").permitAll()
                        .requestMatchers("/getversionmetadata").permitAll()
                        .requestMatchers("/kpiIntegrationValues").permitAll()
						.requestMatchers("/processor/saveRepoToolsStatus").permitAll()
                        .requestMatchers("/v1/kpi/{kpiID}").permitAll()

                        // management metrics
                        .requestMatchers("/info").permitAll().requestMatchers("/health").permitAll().requestMatchers("/env").permitAll()
                        .requestMatchers("/metrics").permitAll()
                        .requestMatchers("/actuator/togglz**").permitAll()
                        .requestMatchers("/togglz-console**").permitAll()
                        .requestMatchers("/actuator**").permitAll()
                        .requestMatchers("/forgotPassword").permitAll()
                        .requestMatchers("/validateEmailToken**").permitAll()
                        .requestMatchers("/resetPassword").permitAll()
                        .requestMatchers("/cache/clearAllCache").permitAll().requestMatchers(HttpMethod.GET, "/cache/clearCache/**")
                        .permitAll().requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/analytics/switch").permitAll().anyRequest().authenticated())
                .addFilterBefore(standardLoginRequestFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(corsFilter(), ChannelProcessingFilter.class)
                .httpBasic(basic -> basic.authenticationEntryPoint(customAuthenticationEntryPoint()))
                .exceptionHandling(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    protected CorsFilter corsFilter() {
        return new CorsFilter();
    }

    protected void setAuthenticationProvider(AuthenticationManagerBuilder auth) {
        List<AuthType> authenticationProviders = authProperties.getAuthenticationProviders();

        if (authenticationProviders.contains(AuthType.STANDARD)) {
            auth.authenticationProvider(standardAuthenticationProvider);
        }
    }

    @Bean
    protected StandardLoginRequestFilter standardLoginRequestFilter(AuthenticationManager authenticationManager){
        return new StandardLoginRequestFilter("/login", authenticationManager, authenticationResultHandler,
                customAuthenticationFailureHandler, customApiConfig, authTypesConfigService);
    }

    @Bean
    CorsConfigurationSource apiConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(customApiConfig.getCorsFilterValidOrigin());
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**",
                "/configuration/security", "/swagger-ui/**", "/webjars/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}