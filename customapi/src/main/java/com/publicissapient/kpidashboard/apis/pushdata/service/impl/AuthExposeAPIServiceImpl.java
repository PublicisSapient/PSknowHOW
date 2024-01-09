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

package com.publicissapient.kpidashboard.apis.pushdata.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataTraceLog;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenRequestDTO;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenResponseDTO;
import com.publicissapient.kpidashboard.apis.pushdata.repository.ExposeApiTokenRepository;
import com.publicissapient.kpidashboard.apis.pushdata.service.AuthExposeAPIService;
import com.publicissapient.kpidashboard.apis.pushdata.service.PushDataTraceLogService;
import com.publicissapient.kpidashboard.common.util.Encryption;
import com.publicissapient.kpidashboard.common.util.EncryptionException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthExposeAPIServiceImpl implements AuthExposeAPIService {
	private static final String TOKEN_KEY = "Api-Key";
	final ModelMapper modelMapper = new ModelMapper();
	@Autowired
	private ExposeApiTokenRepository exposeApiTokenRepository;
	@Autowired
	private ProjectAccessManager projectAccessManager;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private PushDataTraceLogService pushDataTraceLogService;

	/**
	 * user can only one generate token per project and user wise. if same user
	 * request again generate token for same project then previously generated token
	 * will be updated
	 * 
	 * @param exposeAPITokenRequestDTO
	 * @return
	 */
	@Override
	public ServiceResponse generateAndSaveToken(ExposeAPITokenRequestDTO exposeAPITokenRequestDTO) {
		ExposeAPITokenResponseDTO exposeAPITokenResponseDTO = new ExposeAPITokenResponseDTO();
		ExposeApiToken exposeApiTokenExist = exposeApiTokenRepository.findByUserNameAndBasicProjectConfigId(
				exposeAPITokenRequestDTO.getUserName(),
				new ObjectId(exposeAPITokenRequestDTO.getBasicProjectConfigId()));
		String apiAccessToken = "";
		try {
			apiAccessToken = Encryption.getStringKey();
			if (Objects.nonNull(exposeApiTokenExist)) {
				exposeApiTokenExist.setApiToken(apiAccessToken);
				exposeApiTokenExist
						.setExpiryDate(LocalDate.now().plusDays(customApiConfig.getExposeAPITokenExpiryDays()));
				exposeApiTokenExist.setUpdatedAt(LocalDate.now());
				exposeApiTokenRepository.save(exposeApiTokenExist);
				exposeAPITokenResponseDTO = modelMapper.map(exposeApiTokenExist, ExposeAPITokenResponseDTO.class);
				return new ServiceResponse(true, "API token Is updated , All previously generated tokens will expiry",
						exposeAPITokenResponseDTO);
			} else {
				ExposeApiToken exposeApiTokenNew = new ExposeApiToken();
				exposeApiTokenNew.setUserName(exposeAPITokenRequestDTO.getUserName());
				exposeApiTokenNew
						.setExpiryDate(LocalDate.now().plusDays(customApiConfig.getExposeAPITokenExpiryDays()));
				exposeApiTokenNew.setCreatedAt(LocalDate.now());
				exposeApiTokenNew
						.setBasicProjectConfigId(new ObjectId(exposeAPITokenRequestDTO.getBasicProjectConfigId()));
				exposeApiTokenNew.setProjectName(exposeAPITokenRequestDTO.getProjectName());
				exposeApiTokenNew.setApiToken(apiAccessToken);
				exposeApiTokenRepository.save(exposeApiTokenNew);
				exposeAPITokenResponseDTO = modelMapper.map(exposeApiTokenNew, ExposeAPITokenResponseDTO.class);
				return new ServiceResponse(true, "API token is generated Successfully", exposeAPITokenResponseDTO);
			}
		} catch (EncryptionException e) {
			return new ServiceResponse(false, "Error while Creating token", null);
		}
	}

	/**
	 * check valid token and expiry of token
	 * 
	 * @param request
	 * @return ExposeApiToken
	 */
	@Override
	public ExposeApiToken validateToken(HttpServletRequest request) {
		String token = request.getHeader(TOKEN_KEY);
		PushDataTraceLog instance = PushDataTraceLog.getInstance();
		instance.setRequestTime(LocalDateTime.now().toString());
		ExposeApiToken exposeApiToken = exposeApiTokenRepository.findByApiToken(token);
		if (exposeApiToken == null) {
			pushDataTraceLogService.setExceptionTraceLog(
					"Generate Token Push Data via KnowHow tool configuration screen", HttpStatus.UNAUTHORIZED);
		}
		checkProjectAccessPermission(exposeApiToken, instance);
		checkExpiryToken(exposeApiToken);
		exposeApiToken
				.setExpiryDate(exposeApiToken.getExpiryDate().plusDays(customApiConfig.getExposeAPITokenExpiryDays()));
		exposeApiToken.setUpdatedAt(LocalDate.now());
		return exposeApiToken;
	}

	private void checkProjectAccessPermission(ExposeApiToken exposeApiToken, PushDataTraceLog traceLog) {
		traceLog.setProjectName(exposeApiToken.getProjectName());
		traceLog.setBasicProjectConfigId(exposeApiToken.getBasicProjectConfigId());
		traceLog.setUserName(exposeApiToken.getUserName());
		if (!projectAccessManager.hasProjectEditPermission(exposeApiToken.getBasicProjectConfigId(),
				exposeApiToken.getUserName())) {
			pushDataTraceLogService.setExceptionTraceLog("Permission Denied", HttpStatus.UNAUTHORIZED);
		}
	}

	private void checkExpiryToken(ExposeApiToken exposeApiToken) {
		if (exposeApiToken.getExpiryDate().isBefore(LocalDate.now())) {
			pushDataTraceLogService.setExceptionTraceLog("Token Expired, Please Generate New Token",
					HttpStatus.UNAUTHORIZED);
		}
	}

}
