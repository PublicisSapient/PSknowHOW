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
package com.publicissapient.kpidashboard.common.feature;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.togglz.core.manager.FeatureManager;

/**
 * @author purgupta2
 */
@Aspect
@Component
@Slf4j
public class FeaturesAspect {

    @Autowired
    FeatureManager manager;

    @Around(
            "@within(featureAssociation) || @annotation(featureAssociation)"
    )
    public Object checkAspect(ProceedingJoinPoint joinPoint,
                              FeatureAssociation featureAssociation) throws Throwable {

        if (featureAssociation.value().isActive()) {
            return joinPoint.proceed();
        } else {
            log.info(
                    "Feature " + featureAssociation.value().name() + " is not enabled!");
            return null;
        }
    }
}
