package com.publicissapient.kpidashboard.apis.common.service;

import javax.servlet.http.HttpServletResponse;

public interface PushDataValidationService {
    String validateToken(HttpServletResponse response);
}
