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

package com.publicissapient.kpidashboard.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import com.publicissapient.kpidashboard.common.model.generic.ErrorProcessorItem;

import lombok.extern.slf4j.Slf4j;

/**
 * This class prints error log containg information of applicationName, module,
 * error message and the cause.
 *
 */
@Slf4j
@Component
public class ProcessorErrorHandler implements ErrorHandler {

	@Value("${spring.application.name:}")
	private String applicationName;

	@Override
	public void handleError(Throwable th) {
		log.error(ErrorProcessorItem.getErrorItem(applicationName, applicationName, th.getMessage(), th.getCause()));
	}

}
