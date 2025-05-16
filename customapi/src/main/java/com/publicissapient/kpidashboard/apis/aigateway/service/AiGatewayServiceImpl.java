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

package com.publicissapient.kpidashboard.apis.aigateway.service;

import com.publicissapient.kpidashboard.apis.aigateway.client.AiGatewayClient;
import com.publicissapient.kpidashboard.apis.aigateway.dto.request.ChatGenerationRequestDTO;
import com.publicissapient.kpidashboard.apis.aigateway.dto.response.AiProvidersResponseDTO;
import com.publicissapient.kpidashboard.apis.aigateway.dto.response.ChatGenerationResponseDTO;
import com.publicissapient.kpidashboard.apis.errors.ApiClientException;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import retrofit2.Callback;

import java.io.IOException;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class AiGatewayServiceImpl implements AiGatewayService {

    private static final String DEFAULT_AI_PROVIDER_NAME = "openai";

    private final AiGatewayClient aiGatewayClient;

    @Override
    public AiProvidersResponseDTO getProviders() {
        try {
            return aiGatewayClient.getProviders().execute().body();
        }
        catch (IOException e) {
            log.error("Could not process the get providers call {}", e.getMessage());
            throw new ApiClientException("Could not retrieve the Ai providers");
        }
    }

    @Override
    public ChatGenerationResponseDTO generateChatResponse(
            @NotEmpty String prompt
    ) {
        try {
            return aiGatewayClient.generate(
                            new ChatGenerationRequestDTO(prompt, DEFAULT_AI_PROVIDER_NAME)
                    )
                    .execute()
                    .body();
        }
        catch (IOException e) {
            log.error("Could not process the generate chat response call {}", e.getMessage());
            throw new ApiClientException("Could not process the generate chat response");
        }
    }

    @Override
    public void generateChatResponseAsync(@NotEmpty String prompt, Callback<ChatGenerationResponseDTO> callBack) {
        aiGatewayClient.generate(new ChatGenerationRequestDTO(prompt, DEFAULT_AI_PROVIDER_NAME)).enqueue(callBack);
    }
}
