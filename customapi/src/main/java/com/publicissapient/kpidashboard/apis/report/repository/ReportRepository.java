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

package com.publicissapient.kpidashboard.apis.report.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import com.publicissapient.kpidashboard.apis.report.domain.KPI;
import com.publicissapient.kpidashboard.apis.report.domain.Report;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Repository
@Validated
public interface ReportRepository extends MongoRepository<Report, String> {

	Page<Report> findByCreatedBy(String createdBy, Pageable pageable);

	@Query("{ 'name': ?0, 'kpis': { $all: ?1 }, 'createdBy': ?2 }")
	Optional<Report> findByNameAndCreatedByAndKpis(String name, List<KPI> kpis, String createdBy);

	Optional<Report> findByNameAndCreatedBy(
			@NotNull(message = "Report name cannot be null") @NotEmpty(message = "Report name cannot be empty") String name,
			String createdBy);
}
