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

package com.publicissapient.kpidashboard.apis.pschat.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pschat.dto.enums.AssistantType;
import com.publicissapient.kpidashboard.apis.pschat.dto.enums.GPTModel;
import com.publicissapient.kpidashboard.apis.pschat.model.ChatDTO;
import com.publicissapient.kpidashboard.apis.pschat.model.PromptRequest;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.web.client.HttpClientErrorException;


public interface PsChatService {
	public ChatDTO getChat(String chatId) throws HttpClientErrorException;

	public ChatDTO sendPrompt(PromptRequest promptRequest) throws HttpClientErrorException;

	public String getRecommendationForPrompt(GPTModel gptModel, AssistantType assistantType, String prompt)
			throws InvalidRequestException;

	public ServiceResponse getIterationPrompt(GPTModel gptModel, AssistantType assistantType, String sprintId)
			throws InvalidRequestException;
}
