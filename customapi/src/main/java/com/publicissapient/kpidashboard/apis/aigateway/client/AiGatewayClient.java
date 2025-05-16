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

package com.publicissapient.kpidashboard.apis.aigateway.client;

import com.publicissapient.kpidashboard.apis.aigateway.dto.request.ChatGenerationRequestDTO;
import com.publicissapient.kpidashboard.apis.aigateway.dto.response.AiProvidersResponseDTO;
import com.publicissapient.kpidashboard.apis.aigateway.dto.response.ChatGenerationResponseDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AiGatewayClient {
    @POST("/api/aigateway/ai/generate")
    Call<ChatGenerationResponseDTO> generate(
            @Body ChatGenerationRequestDTO chatGenerationResponseDTO
    );

    @GET("/api/aigateway/ai/providers")
    Call<AiProvidersResponseDTO> getProviders();
}