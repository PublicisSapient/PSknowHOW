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
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * object used to bind iteration kpi's value
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@ToString
public class IterationKpiValue implements Serializable {
	private static final long serialVersionUID = 1L;

	private String filter1;
	private String filter2;
	private List<IterationKpiData> data;
	private List<DataCount> value;
	private List<DataCountGroup> dataGroup;
	private List<String> metaDataColumns;
	private List<String> additionalGroup;
	private Map<String, String> markerInfo;
	private transient Map<String, Object> additionalInfo;

	public IterationKpiValue(String filter1, String filter2, List<IterationKpiData> data) {
		this.filter1 = filter1;
		this.filter2 = filter2;
		this.data = data;
	}

	public IterationKpiValue(String filter1, List<DataCount> value) {
		this.filter1 = filter1;
		this.value = value;
	}

	public IterationKpiValue(String filter1, String filter2, List<IterationKpiData> data, List<String> metaDataColumns,
			Map<String, String> markerInfo) {
		this.filter1 = filter1;
		this.filter2 = filter2;
		this.data = data;
		this.metaDataColumns = metaDataColumns;
		this.markerInfo = markerInfo;
	}

}
