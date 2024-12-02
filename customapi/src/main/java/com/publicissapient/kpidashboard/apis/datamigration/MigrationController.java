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
package com.publicissapient.kpidashboard.apis.datamigration;

import java.util.List;

import com.publicissapient.kpidashboard.apis.datamigration.model.MigrateData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

@RestController
@RequestMapping("/hierarchy/migrate")
public class MigrationController {

	@Autowired
	DataMigrationService dataMigrationService;

	@GetMapping(value = "/validate")
	public ResponseEntity<ServiceResponse> dataQualityCheck() {
		List<MigrateData> faultyProjects = dataMigrationService.dataMigration();
		if (CollectionUtils.isNotEmpty(faultyProjects)) {
			return ResponseEntity.status(HttpStatus.SC_METHOD_FAILURE)
					.body(new ServiceResponse(false, "Erroneous Projects", faultyProjects));
		} else {
			return ResponseEntity.status(HttpStatus.SC_NO_CONTENT).body(new ServiceResponse());
		}
	}

	@PutMapping(value = "/populateorganization")//put call
	public ResponseEntity<ServiceResponse> populateOrganizationHierarchy() {
		try {
			dataMigrationService.populateOrganizationHierarchy();
			return ResponseEntity.status(HttpStatus.SC_NO_CONTENT).body(new ServiceResponse());
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
					.body(new ServiceResponse(false, "could not save to database", null));
		}

	}

}
