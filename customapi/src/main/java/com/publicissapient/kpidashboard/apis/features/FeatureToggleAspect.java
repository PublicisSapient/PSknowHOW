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

package com.publicissapient.kpidashboard.apis.features;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.util.NamedFeature;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Aspect
@Component
@Slf4j
public class FeatureToggleAspect {

	@Autowired
	private FeatureManager featureManager;

	@Around("@annotation(enableFeatureToggle)")
	public Object invoke(ProceedingJoinPoint joinPoint, EnableFeatureToggle enableFeatureToggle) throws Throwable {

		if (featureManager.isActive(new NamedFeature(enableFeatureToggle.name()))) {
			log.info(enableFeatureToggle.name() + " feature is Enabled");
			return joinPoint.proceed();
		} else {
			String msg = enableFeatureToggle.name() + " feature is Disabled";
			log.info(msg);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ServiceResponse(false, msg, null));
		}

	}
}
