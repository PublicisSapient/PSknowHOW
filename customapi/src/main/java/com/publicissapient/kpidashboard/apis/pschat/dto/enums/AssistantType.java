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

package com.publicissapient.kpidashboard.apis.pschat.dto.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AssistantType {
	DEFAULT_ASSISTANT("You are a helpful AI assistant. If you do not know the answer to a question, "
			+ "respond by saying \"I do not know the answer to your question\". Respond in markdown "
			+ "format when possible.code parts should start with ```langauge and end with ``` . "
			+ "if asked only give the answer not the code"), PS_CHAT_ASSISTANT(
					"You are a helpful AI assistant. If you do not know the answer to a question, "
							+ "respond by saying \"I do not know the answer to your question\". Respond in short & to the point "
							+ "format when possible.response parts should be short & concise "
							+ "if asked any prompt refer to the psSprintDetails which has been send earlier for response"),;

	private final String role;

	AssistantType(String role) {
		this.role = role;
	}

	@JsonValue
	public String getRole() {
		return role;
	}
}
