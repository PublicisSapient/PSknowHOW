/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.report.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.report.dto.ReportRequest;
import com.publicissapient.kpidashboard.apis.report.service.ReportService;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/** Controller class for managing reports. */
@RestController
@RequestMapping("/reports")
@Slf4j
@Validated
public class ReportController {

	private final ReportService reportService;

	@Autowired
	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	/**
	 * Creates a new report.
	 *
	 * @param report
	 *          the report data transfer object
	 * @return the service response containing the created report
	 */
	@PostMapping
	public ServiceResponse create(@Valid @RequestBody ReportRequest report) {
		log.info("Received request to create a report with name: {}", CommonUtils.sanitize(report.getName()));
		return reportService.create(report);
	}

	/**
	 * Updates an existing report.
	 *
	 * @param id
	 *          the report ID
	 * @param report
	 *          the report data transfer object
	 * @return the service response containing the updated report
	 * @throws EntityNotFoundException
	 *           if the report is not found
	 */
	@PutMapping("/{id}")
	public ServiceResponse update(@PathVariable String id, @Valid @RequestBody ReportRequest report)
			throws EntityNotFoundException {
		log.info("Received request to update report with ID: {}", CommonUtils.sanitize(id));
		return reportService.update(id, report);
	}

	/**
	 * Deletes a report by ID.
	 *
	 * @param id
	 *          the report ID
	 * @return a response entity with no content
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable String id) {
		log.info("Received request to delete report with ID: {}", CommonUtils.sanitize(id));
		reportService.delete(id);
		log.debug("Report deleted successfully with ID: {}", CommonUtils.sanitize(id));
		return ResponseEntity.noContent().build();
	}

	/**
	 * Fetches a report by ID.
	 *
	 * @param id
	 *          the report ID
	 * @return the service response containing the report
	 */
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public ServiceResponse getReportById(@PathVariable String id) {
		log.info("Received request to fetch report by ID: {}", CommonUtils.sanitize(id));
		return reportService.getReportById(id);
	}

	/**
	 * Fetches reports by createdBy with pagination.
	 *
	 * @param createdBy
	 *          the report createdBy
	 * @param page
	 *          the page number
	 * @param limit
	 *          the page size
	 * @return the service response containing a page of reports
	 */
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ServiceResponse getReportsByCreatedBy(@RequestParam String createdBy,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
		log.info("Received request to fetch reports by createdBy: {}, page: {}, size: {}", CommonUtils.sanitize(createdBy),
				page, limit);
		return reportService.getReportsByCreatedBy(createdBy, page, limit);
	}
}
