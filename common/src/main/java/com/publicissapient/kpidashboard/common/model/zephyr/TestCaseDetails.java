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
package com.publicissapient.kpidashboard.common.model.zephyr;

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Data;

@Data
@Document(collection = "test_case_details")
public class TestCaseDetails extends BasicModel {

	private ObjectId processorId;
	private String number;
	private String originalTypeName;
	private String typeName;
	private List<String> labels;
	private String createdDate;
	private String updateDate;
	private String projectName;
	private String projectID;
	private String basicProjectConfigId;
	private String name;
	/*
	 * Automated Test Data
	 */
	private String testAutomated;
	private String isTestAutomated;
	private String isTestCanBeAutomated;
	private String testCaseFolderName;
	private String testAutomatedDate;
	private Set<String> defectStoryID;
	private String defectRaisedBy;
	private String testCaseStatus;

}