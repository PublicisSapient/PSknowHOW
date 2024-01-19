package com.publicissapient.kpidashboard.common.feature;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;

@Configuration
public class TestTogglzConfig {

    @Bean
    @Primary
    public FeatureManager featureManager() {
        return new FeatureManagerBuilder()
                .featureEnum(FeatureEnum.class)
                .build();
    }
}
