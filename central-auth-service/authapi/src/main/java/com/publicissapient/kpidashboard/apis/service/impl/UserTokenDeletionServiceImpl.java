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

package com.publicissapient.kpidashboard.apis.service.impl;

import com.publicissapient.kpidashboard.apis.repository.UserTokenRepository;
import com.publicissapient.kpidashboard.apis.service.UserTokenDeletionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link UserTokenDeletionService}
 * 
 * @author anisingh4
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserTokenDeletionServiceImpl implements UserTokenDeletionService {
	private final UserTokenRepository userTokenRepository;

	@Override
	@Transactional
	public void invalidateSession(String userName) {
		userTokenRepository.deleteAllByUsername(userName);
		log.info("UserTokenDeletionServiceImpl::All tokens are deleted for given User.");
	}
}
