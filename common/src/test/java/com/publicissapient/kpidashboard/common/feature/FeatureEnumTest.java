package com.publicissapient.kpidashboard.common.feature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.togglz.core.manager.FeatureManager;

import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestTogglzConfig.class)
public class FeatureEnumTest {

    @Autowired
    private FeatureManager featureManager;

    @Test
    public void testDailyStandupFeatureIsActive() {
        assertTrue(featureManager.isActive(FeatureEnum.DAILY_STANDUP));
    }

}