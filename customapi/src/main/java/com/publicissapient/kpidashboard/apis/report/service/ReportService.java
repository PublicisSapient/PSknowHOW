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

import com.publicissapient.kpidashboard.apis.errors.DuplicateKpiException;
import com.publicissapient.kpidashboard.apis.errors.DuplicateReportException;
import com.publicissapient.kpidashboard.apis.errors.ReportNotFoundException;
import com.publicissapient.kpidashboard.apis.report.dto.KPIDTO;
import com.publicissapient.kpidashboard.apis.report.dto.ReportDTO;
import com.publicissapient.kpidashboard.apis.report.entity.KPI;
import com.publicissapient.kpidashboard.apis.report.entity.Report;
import com.publicissapient.kpidashboard.apis.report.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Service class for managing reports.
 * @author : girpatha
 */
@Service
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * Creates a new report.
     *
     * @param reportDTO the report data transfer object
     * @return the created report data transfer object
     * @throws DuplicateReportException if a report with the same attributes already exists
     */
    public ReportDTO createReport(ReportDTO reportDTO) {
        log.info("Creating a new report with name: {}", reportDTO.getName());
        // Checking for duplicate KPI IDs
        validateKpiIds(reportDTO.getKpis());
        // Checking if a report with the same attributes already exists
        Optional<Report> existingReport = findExistingReport(reportDTO);
        if (existingReport.isPresent()) {
            log.warn("Report already exists with ID: {}", existingReport.get().getId());
            throw new DuplicateReportException("Report already exists with ID: "+ existingReport.get().getId());
        }
        final ModelMapper modelMapper = new ModelMapper();
        Report report = modelMapper.map(reportDTO, Report.class);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        report = reportRepository.save(report);
        log.debug("Report created successfully with ID: {}", report.getId());
        return modelMapper.map(report, ReportDTO.class);
    }

    /**
     * Updates an existing report.
     *
     * @param id        the report ID
     * @param reportDTO the report data transfer object
     * @return the updated report data transfer object
     * @throws ReportNotFoundException if the report is not found
     */
    @CacheEvict(value = "reportsByName", allEntries = true)
    public ReportDTO updateReport(String id, ReportDTO reportDTO) {
        log.info("Updating report with ID: {}", id);
        // Checking for duplicate KPI IDs
        validateKpiIds(reportDTO.getKpis());
        final ModelMapper modelMapper = new ModelMapper();
        Report existingReport = reportRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Report not found with ID: {}", id);
                    return new ReportNotFoundException("Report not found");
                });
        modelMapper.map(reportDTO, existingReport);
        existingReport.setUpdatedAt(LocalDateTime.now());
        existingReport = reportRepository.save(existingReport);
        log.debug("Report updated successfully with ID: {}", existingReport.getId());
        return modelMapper.map(existingReport, ReportDTO.class);
    }

    /**
     * Deletes a report by ID.
     *
     * @param id the report ID
     */
    @CacheEvict(value = "reportsById", key = "#id")
    public void deleteReport(String id) {
        log.info("Deleting report with ID: {}", id);
        reportRepository.deleteById(id);
        log.debug("Report deleted successfully with ID: {}", id);
    }


    /**
     * Validates the KPI IDs to ensure there are no duplicates.
     *
     * @param kpis the list of KPI data transfer objects
     * @throws DuplicateKpiException if a duplicate KPI ID is found
     */
    private void validateKpiIds(List<KPIDTO> kpis) {
        Set<String> kpiIds = new HashSet<>();
        for (KPIDTO kpi : kpis) {
            if (!kpiIds.add(kpi.getId())) {
                log.error("Duplicate KPI ID found: {}", kpi.getId());
                throw new DuplicateKpiException("Duplicate KPI ID: " + kpi.getId());
            }
        }
    }

    /**
     * Finds an existing report with the same attributes.
     *
     * @param reportDTO the report data transfer object
     * @return an optional containing the existing report if found, otherwise empty
     */
    private Optional<Report> findExistingReport(ReportDTO reportDTO) {
        // Convert KPIDTO to KPI
        final ModelMapper modelMapper = new ModelMapper();
        List<KPI> kpis = reportDTO.getKpis()
                .stream()
                .map(kpiDTO -> modelMapper.map(kpiDTO, KPI.class))
                .toList();
        // Convert kpis to a Set for order-independent comparison
        Set<KPI> kpisSet = new HashSet<>(kpis);
        // Use the custom repository method to find a matching report
        return reportRepository.findByNameAndKpis(reportDTO.getName(), new ArrayList<>(kpisSet));
    }

    /**
     * Fetches a report by ID.
     *
     * @param id the report ID
     * @return the report data transfer object
     * @throws ReportNotFoundException if the report is not found
     */
    @Cacheable(value = "reportsById", key = "#id")
    public ReportDTO getReportById(String id) {
        log.info("Fetching report by ID: {}", id);
        final ModelMapper modelMapper = new ModelMapper();
        Optional<Report> reportOptional = reportRepository.findById(id);

        if (reportOptional.isPresent()) {
            Report report = reportOptional.get();
            return modelMapper.map(report, ReportDTO.class);
        } else {
            log.warn("Report not found with ID: {}", id);
            throw new ReportNotFoundException("Report not found with ID: " + id);
        }
    }

    /**
     * Fetches reports by name with pagination.
     *
     * @param name the report name
     * @param page the page number
     * @param size the page size
     * @return a page of report data transfer objects
     * @throws ReportNotFoundException if no reports are found with the given name
     */
    @Cacheable(value = "reportsByName", key = "{#name, #page, #size}")
    public Page<ReportDTO> getReportsByName(String name, int page, int size) {
        log.info("Fetching reports by name: {}, page: {}, size: {}", name, page, size);
        final ModelMapper modelMapper = new ModelMapper();
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reportsPage = reportRepository.findByName(name, pageable);

        if (reportsPage.hasContent()) {
            return reportsPage.map(report -> modelMapper.map(report, ReportDTO.class));
        } else {
            log.warn("No reports found with name: {}", name);
            throw new ReportNotFoundException("No reports found with name: " + name);
        }
    }
}