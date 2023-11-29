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

package com.publicissapient.kpidashboard.common.model.application;//NOPMD

import java.util.List;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Structure of each FieldMapping is defined
 */
@SuppressWarnings("PMD.TooManyFields")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseFieldMappingStructure extends BasicModel {
	private String fieldName;
	private String fieldLabel;
	private String fieldType;
	private String fieldCategory;
	private String toggleLabel;
	private String toggleLabelLeft;
	private String toggleLabelRight;
	private String section;
	private boolean processorCommon;
	private MappingToolTip tooltip;
	private List<Options> options;
	private List<String> filterGroup;
	private String readOnly;

	@Data
	@Getter
	@Setter
	class MappingToolTip {
		String definition;
		String kpiImpacted;
		String toggleDefinition;
	}

	@Data
	@Getter
	@Setter
	class Options {
		String label;
		Object value;
	}

}