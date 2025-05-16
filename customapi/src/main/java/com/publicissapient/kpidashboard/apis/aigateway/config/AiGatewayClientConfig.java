/*
 *  Copyright 2024 <Sapient Corporation>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the
 *  License.
 */

package com.publicissapient.kpidashboard.apis.aigateway.config;

import com.publicissapient.kpidashboard.apis.aigateway.client.AiGatewayClient;
import com.publicissapient.kpidashboard.apis.m2mauth.service.M2MAuthService;
import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
@AllArgsConstructor
public class AiGatewayClientConfig {

    private final AiGatewayConfig aiGatewayConfig;
    private final M2MAuthService m2MAuthService;

    @Bean
    public AiGatewayClient aiGatewayClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        httpClient.interceptors().clear();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header(HttpHeaders.AUTHORIZATION,
                            "Bearer " + m2MAuthService.generateServiceAuthToken(aiGatewayConfig.getAudience()))
                    .build();
            return chain.proceed(request);
        });

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(aiGatewayConfig.getBaseUrl())
                        .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(httpClient.build()).build();

        return retrofit.create(AiGatewayClient.class);
    }
}
