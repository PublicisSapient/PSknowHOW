package com.publicissapient.kpidashboard.apis.pushdata.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenRequestDTO;

public interface AuthExposeAPIService {

	ServiceResponse generateAndSaveToken(ExposeAPITokenRequestDTO exposeAPITokenRequestDTO);

}
