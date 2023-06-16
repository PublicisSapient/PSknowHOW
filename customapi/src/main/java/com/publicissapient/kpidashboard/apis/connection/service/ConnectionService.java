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

package com.publicissapient.kpidashboard.apis.connection.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.connection.Connection;

/**
 *
 * @author dilip
 * 
 * @author jagmongr
 */
public interface ConnectionService {

	/**
	 * Gets all connection.
	 * 
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse getAllConnection();

	/**
	 * Gets all connection for this type.
	 * 
	 * @param type
	 *            for type of the connection.
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse getConnectionByType(String type);

	/**
	 * Creates and save a connection.
	 * 
	 * @param conn
	 *            for details.
	 * 
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse saveConnectionDetails(Connection conn);

	/**
	 * Modifies a connection. Finds connection by @param id and replaces it
	 * with @param conn
	 * 
	 * @param connection
	 *            for details.
	 * @param id
	 *            replaces the connection data present at id.
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse updateConnection(String id, Connection connection);

	/**
	 * deletes a connection. Finds connection by @param id and delete.
	 * 
	 * @param id
	 *            deleted the connection data present at id.
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found.
	 */

	ServiceResponse deleteConnection(String id);

}