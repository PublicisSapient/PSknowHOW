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

package com.publicissapient.kpidashboard.apis.connection.rest;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.connection.service.ConnectionService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.connection.ConnectionDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Rest Controller for all connection requests.
 *
 * @author dilip
 * 
 * @author jagmongr
 */
@RestController
@RequestMapping("/connections")
@Slf4j
public class ConnectionController {

	@Autowired
	private ConnectionService connectionService;

	/**
	 * Fetch all connection data param type.
	 *
	 * @return the connection
	 * 
	 * @param type
	 *            type
	 */

	@GetMapping
	@PreAuthorize("hasPermission(#type,'CONNECTION_ACCESS')")
	public ResponseEntity<ServiceResponse> getAllConnection(
			@RequestParam(name = "type", required = false) String type) {
		if (StringUtils.isEmpty(type)) {
			log.info("Fetching all connection");
			return ResponseEntity.status(HttpStatus.OK).body(connectionService.getAllConnection());

		} else {
			log.info("Fetching type@{}", type);
			return ResponseEntity.status(HttpStatus.OK).body(connectionService.getConnectionByType(type));

		}
	}

	/**
	 * save/add Connection details
	 * 
	 * @param connectionDTO
	 *            request object that is created in the database.
	 * 
	 * 
	 * @return responseEntity with data,message and status
	 */

	@PostMapping
	@PreAuthorize("hasPermission(#connectionDTO,'CONNECTION_ACCESS')")
	public ResponseEntity<ServiceResponse> saveConnectionDetails(@RequestBody ConnectionDTO connectionDTO) {

		final ModelMapper modelMapper = new ModelMapper();
		final Connection conn = modelMapper.map(connectionDTO, Connection.class);

		log.info("created and saved new connection");
		return ResponseEntity.status(HttpStatus.OK).body(connectionService.saveConnectionDetails(conn));
	}

	/**
	 * Modify/Update a connection by id.
	 * 
	 * @param connectionDTO
	 *            request object that replaces the connection data present at id.
	 *
	 * @return responseEntity with data,message and status
	 */
	@PutMapping("/{id}")
	@PreAuthorize("hasPermission(#id,'CONNECTION_ACCESS')")
	public ResponseEntity<ServiceResponse> modifyConnectionById(@PathVariable String id,
			@Valid @RequestBody ConnectionDTO connectionDTO) {
		log.info("conn@{} updated", connectionDTO.getId());
		final ModelMapper modelMapper = new ModelMapper();
		final Connection conn = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK).body(connectionService.updateConnection(id, conn));
	}

	/**
	 * delete a connection by id.
	 * 
	 * @param id
	 *            deleted the connection data present at id.
	 *
	 * @return responseEntity with data,message and status
	 */

	@DeleteMapping("/{id}")
	@PreAuthorize("hasPermission(#id,'CONNECTION_ACCESS')")
	public ResponseEntity<ServiceResponse> deleteConnection(@PathVariable String id) {

		return ResponseEntity.status(HttpStatus.OK).body(connectionService.deleteConnection(id));
	}

}
