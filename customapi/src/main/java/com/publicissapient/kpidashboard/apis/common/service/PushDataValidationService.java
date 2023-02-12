package com.publicissapient.kpidashboard.apis.common.service;

import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;

import javax.servlet.http.HttpServletRequest;

public interface PushDataValidationService {
    ExposeApiToken validateToken(HttpServletRequest response);
}
