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
package com.publicissapient.kpidashboard.apis.datamigration.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.datamigration.model.MigrationLockLog;
import com.publicissapient.kpidashboard.apis.datamigration.repository.MigrationLogRepository;
import com.publicissapient.kpidashboard.apis.datamigration.util.MigrationEnum;

@Component
public class MigrationLockService {

	@Autowired
	private MigrationLogRepository migrationLogRepository;

	public boolean checkPreviousMigration() {
		List<MigrationLockLog> all = migrationLogRepository.findAll();
		Optional<MigrationLockLog> organizationHierarcy = all.stream()
				.filter(lock -> lock.getStepName().equalsIgnoreCase(MigrationEnum.MIGRATION_STEP.name())).findAny();
		return organizationHierarcy.map(MigrationLockLog::isMigrated).orElse(false);
	}

	public void saveToDB(MigrationLockLog log) {
		migrationLogRepository.save(log);
	}
}
