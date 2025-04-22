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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceTest {

	@Mock
	private ReportRepository reportRepository;

	@Mock
	private AuthenticationService authenticationService;

	@InjectMocks
	private ReportService reportService;

	private ReportRequest reportRequest;
	private Report report;
	private KpiRequest kpiRequest;
	private KPI kpi;

	@Before
	public void setUp() {
		kpiRequest = new KpiRequest();
		kpiRequest.setId("kpi1");
		kpiRequest.setChartData("chartData1");
		kpiRequest.setMetadata("metadata1");

		reportRequest = new ReportRequest();
		reportRequest.setName("Report1");
		reportRequest.setKpis(Collections.singletonList(kpiRequest));

		kpi = new KPI();
		kpi.setId("kpi1");
		kpi.setChartData("chartData1");
		kpi.setMetadata("metadata1");

		report = new Report();
		report.setId(new ObjectId("507f1f77bcf86cd799439011")); // Use a valid 24-character hexadecimal string
		report.setName("Report1");
		report.setKpis(Collections.singletonList(kpi));
	}

	@Test
	public void testCreateReport_Success() {
		when(authenticationService.getLoggedInUser()).thenReturn("user1");
		when(reportRepository.findByNameAndCreatedBy(anyString(), anyString()))
				.thenReturn(Optional.empty());
		when(reportRepository.save(any(Report.class))).thenReturn(report);

		ServiceResponse response = reportService.create(reportRequest);

		assertTrue(response.getSuccess());
		assertEquals("Report created successfully", response.getMessage());
		verify(reportRepository, times(1)).save(any(Report.class));
	}

	@Test(expected = DuplicateReportException.class)
	public void testCreateReport_DuplicateName() {
		when(authenticationService.getLoggedInUser()).thenReturn("user1");
		when(reportRepository.findByNameAndCreatedBy(anyString(), anyString()))
				.thenReturn(Optional.of(report));

		reportService.create(reportRequest);
	}

	@Test
	public void testUpdateReport_Success() throws EntityNotFoundException {
		when(reportRepository.findById(anyString())).thenReturn(Optional.of(report));
		when(reportRepository.save(any(Report.class))).thenReturn(report);

		ServiceResponse response = reportService.update("507f1f77bcf86cd799439011", reportRequest);

		assertTrue(response.getSuccess());
		assertEquals("Report updated successfully", response.getMessage());
		verify(reportRepository, times(1)).save(any(Report.class));
	}

	@Test(expected = EntityNotFoundException.class)
	public void testUpdateReport_NotFound() throws EntityNotFoundException {
		when(reportRepository.findById(anyString())).thenReturn(Optional.empty());

		reportService.update("report1", reportRequest);
	}

	@Test
	public void testDeleteReport_Success() {
		doNothing().when(reportRepository).deleteById(anyString());

		reportService.delete("report1");

		verify(reportRepository, times(1)).deleteById("report1");
	}

	@Test
	public void testGetReportById_Success() {
		when(reportRepository.findById(anyString())).thenReturn(Optional.of(report));

		ServiceResponse response = reportService.getReportById("report1");

		assertTrue(response.getSuccess());
		assertEquals("Reports fetched successfully", response.getMessage());
		verify(reportRepository, times(1)).findById("report1");
	}

	@Test(expected = ReportNotFoundException.class)
	public void testGetReportById_NotFound() {
		when(reportRepository.findById(anyString())).thenReturn(Optional.empty());

		reportService.getReportById("report1");
	}

	@Test
	public void testGetReportsByCreatedBy_Success() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Report> reportPage = new PageImpl<>(Collections.singletonList(report), pageable, 1);

		when(reportRepository.findByCreatedBy(anyString(), any(Pageable.class))).thenReturn(reportPage);

		ServiceResponse response = reportService.getReportsByCreatedBy("user1", 0, 10);

		assertTrue(response.getSuccess());
		assertEquals("Reports fetched successfully", response.getMessage());
		verify(reportRepository, times(1)).findByCreatedBy("user1", pageable);
	}

	@Test(expected = ReportNotFoundException.class)
	public void testGetReportsByCreatedBy_NotFound() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Report> reportPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

		when(reportRepository.findByCreatedBy(anyString(), any(Pageable.class))).thenReturn(reportPage);

		reportService.getReportsByCreatedBy("user1", 0, 10);
	}
}
