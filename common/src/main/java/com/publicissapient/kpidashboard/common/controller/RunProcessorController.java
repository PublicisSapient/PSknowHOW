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

package com.publicissapient.kpidashboard.common.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;

/**
 * Rest Controller to handle bit bucket specific requests.
 *
 * @author swati.lamba
 */
@RestController
public class RunProcessorController {

	private static final ExecutorService PROCESSOR_EXECUTORS = Executors.newFixedThreadPool(5);

	private static final Logger LOGGER = LoggerFactory.getLogger(RunProcessorController.class);

	@Autowired(required = false)
	private ProcessorJobExecutor<?> jobExecuter;


	/**
	 * Run processor
	 * @param projectsBasicConfigIds
	 * @return processors running status
	 */
	@RequestMapping(value = "/processor/run", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<Map> runProcessorForProjects(@RequestBody List<String> projectsBasicConfigIds) {
		MDC.put("Processor Name", jobExecuter.getProcessor().getProcessorName());
		MDC.put("RequestStartTime", String.valueOf(System.currentTimeMillis()));
		LOGGER.info("Received request to run the processor: {} for projects {}",
				jobExecuter.getProcessor().getProcessorName(), projectsBasicConfigIds);

		jobExecuter.setProjectsBasicConfigIds(projectsBasicConfigIds);

		PROCESSOR_EXECUTORS.execute(jobExecuter);

		MDC.put("RequestEndTime", String.valueOf(System.currentTimeMillis()));
		LOGGER.info("Processor execution called");
		MDC.clear();
		Map response = new HashMap();
		response.put("status", "processing");
		return ResponseEntity.ok().body(response);
	}

}
