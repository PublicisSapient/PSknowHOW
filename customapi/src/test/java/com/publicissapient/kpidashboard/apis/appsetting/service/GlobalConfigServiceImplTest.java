package com.publicissapient.kpidashboard.apis.appsetting.service;


import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GlobalConfigServiceImplTest {

    @Mock
    private GlobalConfigRepository globalConfigRepository;

    @InjectMocks
    private GlobalConfigServiceImpl globalConfigService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetZephyrCloudUrlDetailsWhenGlobalConfigsEmpty() {
        ServiceResponse result = globalConfigService.getZephyrCloudUrlDetails();
        assertFalse(result.getSuccess());
        assertEquals("Fetched Zephyr Cloud Base Url successfully", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void testGetZephyrCloudUrlDetailsWhenZephyrCloudBaseUrlIsNull() {
        // Arrange
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setZephyrCloudBaseUrl(null);
        // Act
        ServiceResponse result = globalConfigService.getZephyrCloudUrlDetails();

        // Assert
        assertFalse(result.getSuccess());
        assertEquals("Fetched Zephyr Cloud Base Url successfully", result.getMessage());
        assertNull(result.getData());
    }

}