package com.publicissapient.kpidashboard.common.feature;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

@Configuration
public class ToggleConfiguration {

    @Bean
    public UserProvider userProvider() {
        return new NoOpUserProvider();
    }
}

