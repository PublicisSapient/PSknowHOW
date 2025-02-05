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
import com.publicissapient.kpidashboard.apis.report.entity.Report;
import com.publicissapient.kpidashboard.apis.report.repository.ReportRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private ModelMapper modelMapper;

    @Before
    public void setUp() {
        modelMapper = new ModelMapper();
        reportService = new ReportService(reportRepository);
    }

    @Test
    public void testCreateReport_Success() {
        // Arrange
        ReportDTO reportDTO = createSampleReportDTO();
        Report report = modelMapper.map(reportDTO, Report.class);
        when(reportRepository.findByNameAndKpis(anyString(), anyList())).thenReturn(Optional.empty());
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        // Act
        ReportDTO createdReport = reportService.createReport(reportDTO);

        // Assert
        assertNotNull(createdReport);
        assertEquals(reportDTO.getName(), createdReport.getName());
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test(expected = DuplicateReportException.class)
    public void testCreateReport_DuplicateReport() {
        // Arrange
        ReportDTO reportDTO = createSampleReportDTO();
        Report existingReport = modelMapper.map(reportDTO, Report.class);
        when(reportRepository.findByNameAndKpis(anyString(), anyList())).thenReturn(Optional.of(existingReport));

        // Act
        reportService.createReport(reportDTO);
    }

    @Test(expected = DuplicateKpiException.class)
    public void testCreateReport_DuplicateKpiIds() {
        // Arrange
        ReportDTO reportDTO = createSampleReportDTO();
        KPIDTO duplicateKpi = new KPIDTO();
        duplicateKpi.setId("kpi11");
        duplicateKpi.setChartData("{ \"type\": \"line\", \"data\": { ... } }");
        List<KPIDTO> kpis = new ArrayList<>(reportDTO.getKpis());
        kpis.add(duplicateKpi);
        reportDTO.setKpis(kpis);

        // Act
        reportService.createReport(reportDTO);
    }

    @Test
    public void testUpdateReport_Success() {
        // Arrange
        String reportId = "550e8400-e29b-41d4-a716-446655440000";
        ReportDTO reportDTO = createSampleReportDTO();
        Report existingReport = modelMapper.map(reportDTO, Report.class);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(existingReport));
        when(reportRepository.save(any(Report.class))).thenReturn(existingReport);

        // Act
        ReportDTO updatedReport = reportService.updateReport(reportId, reportDTO);

        // Assert
        assertNotNull(updatedReport);
        assertEquals(reportDTO.getName(), updatedReport.getName());
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test(expected = ReportNotFoundException.class)
    public void testUpdateReport_ReportNotFound() {
        // Arrange
        String reportId = "550e8400-e29b-41d4-a716-446655440000";
        ReportDTO reportDTO = createSampleReportDTO();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act
        reportService.updateReport(reportId, reportDTO);
    }

    @Test
    public void testDeleteReport_Success() {
        // Arrange
        String reportId = "550e8400-e29b-41d4-a716-446655440000";
        doNothing().when(reportRepository).deleteById(reportId);

        // Act
        reportService.deleteReport(reportId);

        // Assert
        verify(reportRepository, times(1)).deleteById(reportId);
    }

    @Test
    public void testGetReportById_Success() {
        // Arrange
        String reportId = "550e8400-e29b-41d4-a716-446655440000";
        Report report = modelMapper.map(createSampleReportDTO(), Report.class);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        ReportDTO fetchedReport = reportService.getReportById(reportId);

        // Assert
        assertNotNull(fetchedReport);
        assertEquals(reportId, fetchedReport.getId());
    }

    @Test(expected = ReportNotFoundException.class)
    public void testGetReportById_ReportNotFound() {
        // Arrange
        String reportId = "550e8400-e29b-41d4-a716-446655440000";
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act
        reportService.getReportById(reportId);
    }

    @Test
    public void testGetReportsByName_Success() {
        // Arrange
        String reportName = "Q3 Report";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<Report> reports = Collections.singletonList(modelMapper.map(createSampleReportDTO(), Report.class));
        Page<Report> reportPage = new PageImpl<>(reports, pageable, reports.size());
        when(reportRepository.findByName(reportName, pageable)).thenReturn(reportPage);

        // Act
        Page<ReportDTO> fetchedReports = reportService.getReportsByName(reportName, page, size);

        // Assert
        assertNotNull(fetchedReports);
        assertEquals(1, fetchedReports.getTotalElements());
    }

    @Test(expected = ReportNotFoundException.class)
    public void testGetReportsByName_NoReportsFound() {
        // Arrange
        String reportName = "NonExistentReport";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        when(reportRepository.findByName(reportName, pageable)).thenReturn(Page.empty());
        // Act
        reportService.getReportsByName(reportName, page, size);
    }

    private ReportDTO createSampleReportDTO() {
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setId("550e8400-e29b-41d4-a716-446655440000");
        reportDTO.setName("Q3 Report");
        reportDTO.setCreatedBy("user-123");
        reportDTO.setUpdatedBy("user-456");
        reportDTO.setCreatedAt(LocalDateTime.now());
        reportDTO.setUpdatedAt(LocalDateTime.now());

        KPIDTO kpiDTO = new KPIDTO();
        kpiDTO.setId("kpi11");
        kpiDTO.setChartData("{ \"type\": \"line\", \"data\": { ... } }");

        // Creating metadata as a Map (since metadata is of type Object)
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", "Defect count by RCA");
        metadata.put("createdAt", LocalDateTime.now().toString());
        kpiDTO.setMetadata(metadata);

        reportDTO.setKpis(Collections.singletonList(kpiDTO));
        return reportDTO;
    }
}