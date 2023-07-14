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

package com.publicissapient.kpidashboard.common.model.generic;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.ToString;

@Builder(access = AccessLevel.PRIVATE, builderMethodName = "errorItemBuilder")
@ToString(includeFieldNames = true)
public class ErrorProcessorItem {
	private String applicationName;
	private String module;
	private String message;
	private Throwable cause;

	public static String getErrorItem(String applicationName, String module, String message, Throwable cause) {
		return ErrorProcessorItem.errorItemBuilder().applicationName(applicationName).module(module).message(message)
				.cause(cause).build().toString();
	}
}
