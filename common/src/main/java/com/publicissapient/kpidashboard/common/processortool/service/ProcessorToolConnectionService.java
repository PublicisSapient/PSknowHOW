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

package com.publicissapient.kpidashboard.common.processortool.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

/**
 * @author narsingh9
 * 
 *         service class for mapping {@link ProjectToolConfig} and
 *         {@link Connection}
 *
 */
public interface ProcessorToolConnectionService {

	/**
	 * Method to get list of {@link ProcessorToolConnection}
	 * 
	 * @param toolName
	 *            name of tool
	 * 
	 * @return list of {@link ProcessorToolConnection}
	 */
	List<ProcessorToolConnection> findByTool(String toolName);

	/**
	 * Method to get list of {@link ProcessorToolConnection}
	 * 
	 * @param toolName
	 *            name of tool
	 * @param configId
	 *            basic project config id
	 * 
	 * @return list of {@link ProcessorToolConnection}
	 */
	List<ProcessorToolConnection> findByToolAndBasicProjectConfigId(String toolName, ObjectId configId);
}
