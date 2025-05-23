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

package com.publicissapient.kpidashboard.apis.report.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.errors.DuplicateReportException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.errors.ReportNotFoundException;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.report.domain.KPI;
import com.publicissapient.kpidashboard.apis.report.domain.Report;
import com.publicissapient.kpidashboard.apis.report.dto.KpiRequest;
import com.publicissapient.kpidashboard.apis.report.dto.ReportRequest;
import com.publicissapient.kpidashboard.apis.report.repository.ReportRepository;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing reports.
 *
 * @author : girpatha
 */
@Service
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public ReportService(ReportRepository reportRepository, AuthenticationService authenticationService) {
        this.reportRepository = reportRepository;
        this.authenticationService = authenticationService;
    }

    /**
     * Creates a new report.
     *
     * @param reportRequest
     *          the report data transfer object
     * @return the created report data transfer object
     * @throws DuplicateReportException
     *           if a report with the same attributes already exists
     */
    public ServiceResponse create(ReportRequest reportRequest) {
        log.info("Creating a new report with name: {}", CommonUtils.sanitize(reportRequest.getName()));
        // Get the current user
        String createdBy = authenticationService.getLoggedInUser();
        // Check if a report with the same name already exists for the current user
        Optional<Report> existingByNameAndCreatedBy = reportRepository.findByNameAndCreatedBy(reportRequest.getName(),
                createdBy);
        if (existingByNameAndCreatedBy.isPresent()) {
            log.warn("Report with the same name already exists for user: {}", CommonUtils.sanitize(createdBy));
            throw new DuplicateReportException("Report with the same name already exists for user: " + createdBy);
        }
        // Checking if a report with the same attributes already exists
        Optional<Report> existingReport = findExistingReport(reportRequest);
        if (existingReport.isPresent()) {
            log.warn("Report already exists with ID: {}", CommonUtils.sanitize(String.valueOf(existingReport.get().getId())));
            throw new DuplicateReportException("Report already exists with ID: " + existingReport.get().getId());
        }
        final ModelMapper modelMapper = new ModelMapper();
        Report report = modelMapper.map(reportRequest, Report.class);
        report = reportRepository.save(report);
        log.debug("Report created successfully with ID: {}", CommonUtils.sanitize(String.valueOf(report.getId())));
        ReportRequest reportReq = modelMapper.map(report, ReportRequest.class);
        return new ServiceResponse(true, "Report created successfully", reportReq);
    }

    /**
     * Updates an existing report.
     *
     * @param id
     *          the report ID
     * @param reportRequest
     *          the report data transfer object
     * @return the updated report data transfer object
     * @throws ReportNotFoundException
     *           if the report is not found
     */
    public ServiceResponse update(String id, ReportRequest reportRequest) throws EntityNotFoundException {
        log.info("Updating report with ID: {}", CommonUtils.sanitize(id));
        final ModelMapper modelMapper = new ModelMapper();
        Report existingReport = reportRepository.findById(id).orElseThrow(() -> {
            log.error("Report not found with ID: {}", CommonUtils.sanitize(id));
            return new EntityNotFoundException(Report.class, "id", id);
        });
        existingReport.setName(reportRequest.getName());
        existingReport.setKpis(new ArrayList<>(existingReport.getKpis()));
        existingReport.getKpis().clear();
        // Update existing KPIs or add new ones
        for (KpiRequest kpiRequest : reportRequest.getKpis()) {
            KPI newKpi = modelMapper.map(kpiRequest, KPI.class);
            existingReport.getKpis().add(newKpi);
        }
        existingReport = reportRepository.save(existingReport);
        log.debug("Report updated successfully with ID: {}", CommonUtils.sanitize(String.valueOf(existingReport.getId())));
        ReportRequest reportReq = modelMapper.map(existingReport, ReportRequest.class);
        return new ServiceResponse(true, "Report updated successfully", reportReq);
    }

    /**
     * Deletes a report by ID.
     *
     * @param id
     *          the report ID
     */
    public void delete(String id) {
        log.info("Deleting report with ID: {}", CommonUtils.sanitize(id));
        reportRepository.deleteById(id);
        log.debug("Report deleted successfully with ID: {}", CommonUtils.sanitize(id));
    }

    /**
     * Finds an existing report with the same attributes.
     *
     * @param reportRequest
     *          the report data transfer object
     * @return an optional containing the existing report if found, otherwise empty
     */
    private Optional<Report> findExistingReport(ReportRequest reportRequest) {
        // Convert KPIDTO to KPI
        final ModelMapper modelMapper = new ModelMapper();
        List<KPI> kpis = reportRequest.getKpis().stream().map(kpiDTO -> modelMapper.map(kpiDTO, KPI.class)).toList();
        // Convert kpis to a Set for order-independent comparison
        Set<KPI> kpisSet = new HashSet<>(kpis);
        // Use the custom repository method to find a matching report
        return reportRepository.findByNameAndCreatedByAndKpis(reportRequest.getName(), new ArrayList<>(kpisSet),
                authenticationService.getLoggedInUser());
    }

    /**
     * Fetches a report by ID.
     *
     * @param id
     *          the report ID
     * @return the report data transfer object
     * @throws ReportNotFoundException
     *           if the report is not found
     */
    public ServiceResponse getReportById(String id) {
        log.info("Fetching report by ID: {}", CommonUtils.sanitize(id));
        Optional<Report> reportOptional = reportRepository.findById(id);
        if (reportOptional.isPresent()) {
            final ModelMapper modelMapper = new ModelMapper();
            Report report = reportOptional.get();
            ReportRequest reportRequest = modelMapper.map(report, ReportRequest.class);
            return new ServiceResponse(true, "Reports fetched successfully", reportRequest);
        } else {
            log.warn("Report not found with ID: {}", CommonUtils.sanitize(id));
            throw new ReportNotFoundException("Report not found with ID: " + id);
        }
    }

    /**
     * Fetches reports by createdBy with pagination.
     *
     * @param createdBy
     *          the report createdBy
     * @param page
     *          the page number
     * @param size
     *          the page size
     * @return a page of report data transfer objects
     * @throws ReportNotFoundException
     *           if no reports are found with the given createdBy
     */
    public ServiceResponse getReportsByCreatedBy(String createdBy, int page, int size) {
        log.info("Fetching reports by createdBy: {}, page: {}, size: {}", CommonUtils.sanitize(createdBy), page, size);
        final ModelMapper modelMapper = new ModelMapper();
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reportsPage = reportRepository.findByCreatedBy(createdBy, pageable);
        if (reportsPage.hasContent()) {
            Page<ReportRequest> reportRequestPage = reportsPage.map(report -> modelMapper.map(report, ReportRequest.class));
            return new ServiceResponse(true, "Reports fetched successfully", reportRequestPage);
        } else {
            log.warn("No reports found with createdBy: {}", CommonUtils.sanitize(createdBy));
            throw new ReportNotFoundException("No reports found with createdBy: " + createdBy);
        }
    }
}
