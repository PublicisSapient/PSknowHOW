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

package com.publicissapient.kpidashboard.common.model.application;

import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for persisting global configuration Global configurations based on
 * environment can have all configurations mapped with their respective
 * keys(name of the configuration).
 */
@Document(collection = "global_config")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalConfig extends BasicModel {

	private String env;

	private AuthTypeStatus authTypeStatus;

	private ADServerDetail adServerDetail;

	private EmailServerDetail emailServerDetail;

	private String zephyrCloudBaseUrl;

}
