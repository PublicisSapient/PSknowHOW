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

package com.publicissapient.kpidashboard.azurepipeline.processor.supplier;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;

/**
 * Supplier that returns an instance of RestOperations.
 */
@Component
public class AzurePipelineRestOperations implements RestOperationsFactory<RestOperations> {

	/**
	 * Instantiate RestTemplate.
	 */
	@Override
	public RestOperations getTypeInstance() {
		return new RestTemplate();
	}
}
