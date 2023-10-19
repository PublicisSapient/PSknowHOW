package com.publicissapient.kpidashboard.common.feature;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.togglz.core.manager.FeatureManager;

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
