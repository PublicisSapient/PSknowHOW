package com.publicissapient.kpidashboard.apis.auth.service;

import com.publicissapient.kpidashboard.common.model.application.AuthTypeConfig;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;

public interface AuthTypesConfigService {

	AuthTypeConfig saveAuthTypeConfig(AuthTypeConfig authTypeConfig);

	AuthTypeConfig getAuthTypeConfig();

	AuthTypeStatus getAuthTypesStatus();
}
