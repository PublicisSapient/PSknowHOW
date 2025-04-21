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

package com.publicissapient.kpidashboard.apis.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * object used to bind iteration kpi's value
 */
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IterationKpiData implements Serializable {
	private static final long serialVersionUID = 398405590574412201L;
	private String label;
	private Double value;
	private Double value1;
	private String labelInfo;
	private String unit;
	private String unit1;
	private List<String> expressions;
	private List<IterationKpiModalValue> modalValues;

	public IterationKpiData(String label, Double value, Double value1, String labelInfo, String unit, String unit1,
			List<IterationKpiModalValue> modalValues) {
		this.label = label;
		this.value = value;
		this.value1 = value1;
		this.labelInfo = labelInfo;
		this.unit = unit;
		this.unit1 = unit1;
		this.modalValues = modalValues;
	}

	public IterationKpiData(String label, Double value, Double value1, String labelInfo, String unit,
			List<IterationKpiModalValue> modalValues) {
		this.label = label;
		this.value = value;
		this.value1 = value1;
		this.labelInfo = labelInfo;
		this.unit = unit;
		this.modalValues = modalValues;
	}

	public IterationKpiData(String label, Double value, Double value1, String labelInfo, String unit,
			List<IterationKpiModalValue> modalValues, List<String> expressions) {
		this.label = label;
		this.value = value;
		this.value1 = value1;
		this.labelInfo = labelInfo;
		this.unit = unit;
		this.modalValues = modalValues;
		this.expressions = expressions;
	}

}
