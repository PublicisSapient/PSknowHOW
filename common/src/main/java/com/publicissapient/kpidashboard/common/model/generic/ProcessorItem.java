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

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a unique processor item in an external tool.
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "processor_items")
public class ProcessorItem extends BasicModel {

	private String desc;
	private boolean isActive;
	private List<ProcessorError> errors = Lists.newArrayList();
	private ObjectId processorId;
	private long updatedTime;
	private Map<String, Object> toolDetailsMap = Maps.newHashMap();

	private Short version;
	private ObjectId toolConfigId;

	@Transient
	private Processor processor;

}
