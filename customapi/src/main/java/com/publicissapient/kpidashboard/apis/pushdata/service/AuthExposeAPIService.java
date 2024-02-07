/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.pushdata.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenRequestDTO;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthExposeAPIService {

	ServiceResponse generateAndSaveToken(ExposeAPITokenRequestDTO exposeAPITokenRequestDTO);

	ExposeApiToken validateToken(HttpServletRequest request);

}
