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
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.publicissapient.kpidashboard.apis.pushdata.service.AuthExposeAPIService;
import lombok.extern.slf4j.Slf4j;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenRequestDTO;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenResponseDTO;
import com.publicissapient.kpidashboard.apis.pushdata.repository.ExposeApiTokenRepository;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;
import com.publicissapient.kpidashboard.common.util.Encryption;
import com.publicissapient.kpidashboard.common.util.EncryptionException;

@Service
@Slf4j
public class AuthExposeAPIServiceImpl implements AuthExposeAPIService {

	private static final long TOKEN_EXPIRY_DAYS = 30L;
	private static final String TOKEN_KEY = "Api-Key";

	@Autowired
	private ExposeApiTokenRepository exposeApiTokenRepository;

	@Autowired
	ProjectAccessManager projectAccessManager;

	final ModelMapper modelMapper = new ModelMapper();

	/**
	 * only one generate token per project and user wise, if user generate token
	 * again for same then existing token and expiry will be updated.
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
				exposeApiTokenExist.setExpiryDate(LocalDate.now().plusDays(TOKEN_EXPIRY_DAYS));
				exposeApiTokenExist.setUpdatedAt(LocalDate.now());
				exposeApiTokenRepository.save(exposeApiTokenExist);
				exposeAPITokenResponseDTO = modelMapper.map(exposeApiTokenExist, ExposeAPITokenResponseDTO.class);
				return new ServiceResponse(true, "API token Is updated , after onward use this token",
						exposeAPITokenResponseDTO);
			} else {
				ExposeApiToken exposeApiTokenNew = new ExposeApiToken();
				exposeApiTokenNew.setUserName(exposeAPITokenRequestDTO.getUserName());
				exposeApiTokenNew.setExpiryDate(LocalDate.now().plusDays(TOKEN_EXPIRY_DAYS));
				exposeApiTokenNew.setCreatedAt(LocalDate.now());
				exposeApiTokenNew
						.setBasicProjectConfigId(new ObjectId(exposeAPITokenRequestDTO.getBasicProjectConfigId()));
				exposeApiTokenNew.setProjectName(exposeAPITokenRequestDTO.getProjectName());
				exposeApiTokenNew.setApiToken(apiAccessToken);
				exposeApiTokenRepository.save(exposeApiTokenNew);
				exposeAPITokenResponseDTO = modelMapper.map(exposeApiTokenNew, ExposeAPITokenResponseDTO.class);
				return new ServiceResponse(true, "Please save this API token for API Call", exposeAPITokenResponseDTO);
			}
		} catch (EncryptionException e) {
			return new ServiceResponse(false, "Error while Creating token", null);
		}
	}

	@Override
	public ExposeApiToken validateToken(HttpServletRequest request) {
		String token = request.getHeader(TOKEN_KEY);
		ExposeApiToken exposeApiToken = exposeApiTokenRepository.findByApiToken(token);
		if (exposeApiToken == null) {
			throw new PushDataException("Create Token To Push Data", HttpStatus.UNAUTHORIZED);
		}
		checkExpiryToken(exposeApiToken);
		checkProjectAccessPermission(exposeApiToken);
		exposeApiToken.setExpiryDate(exposeApiToken.getExpiryDate().plusDays(TOKEN_EXPIRY_DAYS));
		exposeApiToken.setUpdatedAt(LocalDate.now());
		return exposeApiToken;
	}

	private void checkProjectAccessPermission(ExposeApiToken exposeApiToken) {
		if (!projectAccessManager.hasProjectEditPermission(exposeApiToken.getBasicProjectConfigId(),
				exposeApiToken.getUserName() // if not user based, then loggedinuser
		)) {
			throw new PushDataException("Permission Denied", HttpStatus.UNAUTHORIZED);
		}
	}

	private void checkExpiryToken(ExposeApiToken exposeApiToken) {
		if (exposeApiToken.getExpiryDate().isBefore(LocalDate.now())) {
			throw new PushDataException("Token Expired, Please Generate New Token", HttpStatus.UNAUTHORIZED);
		}
	}

}
