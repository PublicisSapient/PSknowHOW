package com.publicissapient.kpidashboard.common.feature;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum FeatureEnum implements Feature {

    @Label("Custom-api feature 1")
    FEATURE_1,

    @Label("Custom-api Daily Standup")
    DAILY_STANDUP,

    @Label("Jira feature")
    FEATURE_3;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
