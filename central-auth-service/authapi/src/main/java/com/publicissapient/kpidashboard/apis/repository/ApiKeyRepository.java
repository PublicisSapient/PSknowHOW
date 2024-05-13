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

package com.publicissapient.kpidashboard.apis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.apis.entity.ApiKey;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
	@Query(value = "select exists (select 1 from auth.api_key where resource_id = (select id from auth.resource where name = :resource) and key = :apiKey );", nativeQuery = true)
	boolean validateApiKeyByResource(@Param("resource") String resource, @Param("apiKey") String apiKey);

	@Query(value = "select * from auth.api_key where resource_id = (select id from auth.resource where name = :resource);", nativeQuery = true)
	ApiKey findByResource(@Param("resource") String resource);

	@Query(value = "select exists (select 1 from auth.api_key where key = :apiKey );", nativeQuery = true)
	boolean validateApiKey(@Param("apiKey") String apiKey);
}
