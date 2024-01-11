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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.publicissapient.kpidashboard.apis.activedirectory.service.ADServerDetailsService;
import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.AuthenticationResultHandler;
import com.publicissapient.kpidashboard.apis.auth.CustomAuthenticationFailureHandler;
import com.publicissapient.kpidashboard.apis.auth.apitoken.ApiTokenAuthenticationProvider;
import com.publicissapient.kpidashboard.apis.auth.apitoken.ApiTokenRequestFilter;
import com.publicissapient.kpidashboard.apis.auth.ldap.CustomUserDetailsContextMapper;
import com.publicissapient.kpidashboard.apis.auth.ldap.LdapLoginRequestFilter;
import com.publicissapient.kpidashboard.apis.auth.service.AuthTypesConfigService;
import com.publicissapient.kpidashboard.apis.auth.standard.StandardLoginRequestFilter;
import com.publicissapient.kpidashboard.apis.auth.token.JwtAuthenticationFilter;
import com.publicissapient.kpidashboard.apis.errors.CustomAuthenticationEntryPoint;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.constant.AuthType;

import static org.springframework.security.config.Customizer.withDefaults;

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
@EnableMethodSecurity
public class WebSecurityConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthenticationResultHandler authenticationResultHandler;

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    private AuthenticationProvider standardAuthenticationProvider;

    @Autowired
    private ApiTokenAuthenticationProvider apiTokenAuthenticationProvider;

    @Autowired
    private AuthProperties authProperties;

    private CustomApiConfig customApiConfig;

    @Autowired
    private ADServerDetailsService adServerDetailsService;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private AuthTypesConfigService authTypesConfigService;

    public static Properties getProps() throws IOException {
        Properties prop = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("crowd.properties")) {
            prop.load(in);
        }
        return prop;
    }

    @Autowired
    public void setCustomApiConfig(CustomApiConfig customApiConfig) {
        this.customApiConfig = customApiConfig;
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
        // Get AuthenticationManager
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        http.headers(headers -> headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable));
        http.httpBasic(basic -> basic.authenticationEntryPoint(customAuthenticationEntryPoint));
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(withDefaults())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/appinfo").permitAll().requestMatchers("/registerUser")
                        .permitAll().requestMatchers("/changePassword").permitAll().requestMatchers("/login/captcha").permitAll()
                        .requestMatchers("/login/captchavalidate").permitAll().requestMatchers("/login**").permitAll()
                        .requestMatchers("/error").permitAll().requestMatchers("/authenticationProviders").permitAll()
                        .requestMatchers("/auth-types-status").permitAll().requestMatchers("/pushData/*").permitAll()
                        .requestMatchers("/getversionmetadata").permitAll()

                        // management metrics
                        .requestMatchers("/info").permitAll().requestMatchers("/health").permitAll().requestMatchers("/env").permitAll()
                        .requestMatchers("/metrics").permitAll()
                        .requestMatchers("/actuator/togglz**").permitAll()
                        .requestMatchers("/togglz-console**").permitAll()
                        .requestMatchers("/actuator**").permitAll().requestMatchers("/forgotPassword").permitAll()
                        .requestMatchers("/validateToken**").permitAll().requestMatchers("/resetPassword").permitAll()
                        .requestMatchers("/cache/clearAllCache").permitAll().requestMatchers(HttpMethod.GET, "/cache/clearCache/**")
                        .permitAll().requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/analytics/switch").permitAll().anyRequest().authenticated())
                .addFilterBefore(standardLoginRequestFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(ldapLoginRequestFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiTokenRequestFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(corsFilterKnowHOW(), ChannelProcessingFilter.class)
                .httpBasic(basic -> basic.authenticationEntryPoint(customAuthenticationEntryPoint))
                .exceptionHandling(Customizer.withDefaults());
        return http.build();
    }

    protected void setAuthenticationProvider(AuthenticationManagerBuilder auth) throws Exception {
        List<AuthType> authenticationProviders = authProperties.getAuthenticationProviders();

        if (authenticationProviders.contains(AuthType.STANDARD)) {
            auth.authenticationProvider(standardAuthenticationProvider);
        }
        ADServerDetail adServerDetail = adServerDetailsService.getADServerConfig();
        if (authenticationProviders.contains(AuthType.LDAP) && adServerDetail != null) {
            auth.ldapAuthentication().userSearchBase(adServerDetail.getRootDn())
                    .userDnPatterns(adServerDetail.getUserDn()).contextSource().url(adServerDetail.getHost())
                    .port(adServerDetail.getPort()).managerDn(adServerDetail.getUsername())
                    .managerPassword(adServerDetail.getPassword()).and().passwordCompare()
                    .passwordAttribute("password");
            auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
        }
        auth.authenticationProvider(apiTokenAuthenticationProvider);
    }

    @Bean
    protected StandardLoginRequestFilter standardLoginRequestFilter(AuthenticationManager authenticationManager) throws Exception {
        return new StandardLoginRequestFilter("/login", authenticationManager, authenticationResultHandler,
                customAuthenticationFailureHandler, customApiConfig, authTypesConfigService);
    }

    // update authenticatoin result handler
    @Bean
    protected LdapLoginRequestFilter ldapLoginRequestFilter(AuthenticationManager authenticationManager) throws Exception {
        return new LdapLoginRequestFilter("/ldap", authenticationManager, authenticationResultHandler,
                customAuthenticationFailureHandler, customApiConfig, adServerDetailsService, authTypesConfigService);
    }

    @Bean
    protected ApiTokenRequestFilter apiTokenRequestFilter(AuthenticationManager authenticationManager) throws Exception {
        return new ApiTokenRequestFilter("/**", authenticationManager, authenticationResultHandler);
    }

    @Bean
    protected CorsFilter corsFilterKnowHOW() throws Exception {// NOSONAR
        return new CorsFilter();
    }

    @Bean
    protected AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        ADServerDetail adServerDetail = adServerDetailsService.getADServerConfig();
        if (adServerDetail == null || StringUtils.isBlank(adServerDetail.getHost())) {
            return null;
        }
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(
                adServerDetail.getDomain(), adServerDetail.getHost(), adServerDetail.getRootDn());
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setUserDetailsContextMapper(new CustomUserDetailsContextMapper());
        return provider;
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
