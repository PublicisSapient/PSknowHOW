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

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.report.dto.ReportDTO;
import com.publicissapient.kpidashboard.apis.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * Controller class for managing reports.
 * @Author : girpatha
 */
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
	 * @param reportDTO the report data transfer object
	 * @return the response entity containing the service response
	 */
	@PostMapping
	public ResponseEntity<ServiceResponse> createReport(@Valid @RequestBody ReportDTO reportDTO) {
		log.info("Received request to create a report with name: {}", reportDTO.getName());
		ServiceResponse response = null;
		ReportDTO createdReport = reportService.createReport(reportDTO);
		if (createdReport != null && !createdReport.toString().isEmpty()) {
			response = new ServiceResponse(true, "Report created successfully", createdReport);
			log.debug("Report created successfully with ID: {}", createdReport.getId());
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			response = new ServiceResponse(false, "", null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}


	/**
	 * Updates an existing report.
	 *
	 * @param id        the report ID
	 * @param reportDTO the report data transfer object
	 * @return the response entity containing the service response
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ServiceResponse> updateReport(@PathVariable String id, @Valid @RequestBody ReportDTO reportDTO) {
		log.info("Received request to update report with ID: {}", id);
		ServiceResponse response = null;
		ReportDTO updatedReport = reportService.updateReport(id, reportDTO);
		if (updatedReport != null && !updatedReport.toString().isEmpty()) {
			response = new ServiceResponse(true, "Report updated successfully", updatedReport);
			log.debug("Report updated successfully with ID: {}", updatedReport.getId());
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			response = new ServiceResponse(false, "", null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

	/**
	 * Deletes a report by ID.
	 *
	 * @param id the report ID
	 * @return the response entity with no content
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<ServiceResponse> deleteReport(@PathVariable String id) {
		log.info("Received request to delete report with ID: {}", id);
		reportService.deleteReport(id);
		log.debug("Report deleted successfully with ID: {}", id);
		return ResponseEntity.noContent().build();
	}


	/**
	 * Fetches a report by ID.
	 *
	 * @param id the report ID
	 * @return the response entity containing the service response
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ServiceResponse> getReportById(@PathVariable String id) {
		log.info("Received request to fetch report by ID: {}", id);
		ServiceResponse response = null;
			ReportDTO reportDTO = reportService.getReportById(id);
			if (reportDTO != null) {
				response = new ServiceResponse(true, "Report fetched successfully", reportDTO);
				log.debug("Report fetched successfully with ID: {}", id);
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				response = new ServiceResponse(false, "Report not found with ID: " + id, null);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
	}


	/**
	 * Fetches reports by name with pagination.
	 *
	 * @param name the report name
	 * @param page the page number
	 * @param size the page size
	 * @return the response entity containing the service response
	 */
	@GetMapping
	public ResponseEntity<ServiceResponse> getReportsByName(
			@RequestParam String name,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		log.info("Received request to fetch reports by name: {}, page: {}, size: {}", name, page, size);
		ServiceResponse response = null;
			Page<ReportDTO> reportsPage = reportService.getReportsByName(name, page, size);
			if (reportsPage.hasContent()) {
				response = new ServiceResponse(true, "Reports fetched successfully", reportsPage);
				log.debug("Reports fetched successfully for name: {}", name);
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				response = new ServiceResponse(false, "No reports found with name: " + name, null);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
	}
}