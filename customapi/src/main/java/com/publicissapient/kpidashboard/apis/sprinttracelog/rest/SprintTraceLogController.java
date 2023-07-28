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

package com.publicissapient.kpidashboard.apis.sprinttracelog.rest;

import com.publicissapient.kpidashboard.apis.sprinttracelog.service.SprintTraceLogService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activeIteration")
public class SprintTraceLogController {
	@Autowired
	SprintTraceLogService sprintTraceLogService;

	@GetMapping("/fetchStatus/{sprintId}")
	public ResponseEntity<ServiceResponse> getActiveItrFetchStatus(@PathVariable String sprintId) {
		ServiceResponse response = sprintTraceLogService.getActiveSprintFetchStatus(sprintId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
