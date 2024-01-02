package com.publicissapient.kpidashboard.common.feature;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.spring.boot.actuate.TogglzEndpoint;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzAutoConfiguration;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {FeaturesAspect.class, TestTogglzConfig.class})
public class FeaturesAspectTest {

    @Autowired
    private FeaturesAspect featuresAspect;




    @Test
    public void testAspectHandlesEnabledFeature() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        FeatureAssociation featureAssociation = mock(FeatureAssociation.class);
        when(featureAssociation.value()).thenReturn(FeatureEnum.DAILY_STANDUP);
        when(featureAssociation.value().isActive()).thenReturn(true);

        // When
        Object result = featuresAspect.checkAspect(joinPoint, featureAssociation);

        // Then
        verify(joinPoint, times(1)).proceed(); // Verify that proceed() is called
    }

    @Test
    public void testAspectHandlesDisabledFeature() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        FeatureAssociation featureAssociation = mock(FeatureAssociation.class);
        when(featureAssociation.value()).thenReturn(FeatureEnum.DAILY_STANDUP);
        when(featureAssociation.value().isActive()).thenReturn(false);

        // When
        Object result = featuresAspect.checkAspect(joinPoint, featureAssociation);

        // Then
        verify(joinPoint, never()).proceed(); // Verify that proceed() is not called
        // You can add additional assertions based on your aspect behavior
    }
}