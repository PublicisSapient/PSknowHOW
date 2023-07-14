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
package com.publicissapient.kpidashboard.apis.activedirectory.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sansharm13
 *
 */
@Service
@Slf4j
public class ADServerDetailsServiceImpl implements ADServerDetailsService {

	private static final String ERROR_MESSAGE_NON_SUPERADMIN = "Super-Admin is authorized to Add/Updated/Get active directory details";
	@Autowired
	private GlobalConfigRepository globalConfigRepository;
	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;
	private String userIdRegex = "uid={0},";
	private String ldapsConnection = "ldaps://";

	@Override
	public ServiceResponse addUpdateADServerDetails(ADServerDetail adUserDetail) {

		if (!authorizedProjectsService.ifSuperAdminUser()) {
			log.error(ERROR_MESSAGE_NON_SUPERADMIN);
			return new ServiceResponse(true, ERROR_MESSAGE_NON_SUPERADMIN, null);
		}

		List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
		String passEncrypt = encryptStringForDb(adUserDetail.getPassword());
		adUserDetail.setPassword(passEncrypt);
		globalConfigs.get(0).setAdServerDetail(adUserDetail);
		globalConfigRepository.saveAll(globalConfigs);
		return new ServiceResponse(true, "created and updated active directory user", adUserDetail);
	}

	@Override
	public ServiceResponse getADServerDetails() {

		if (!authorizedProjectsService.ifSuperAdminUser()) {
			log.error(ERROR_MESSAGE_NON_SUPERADMIN);
			return new ServiceResponse(false, ERROR_MESSAGE_NON_SUPERADMIN, null);
		}

		List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
		GlobalConfig globalConfig = CollectionUtils.isEmpty(globalConfigs) ? null : globalConfigs.get(0);
		ADServerDetail adServerDetail = globalConfig == null ? new ADServerDetail() : globalConfig.getAdServerDetail();
		if (adServerDetail != null) {
			adServerDetail.setPassword(null);
		} else {
			return new ServiceResponse(false, "No record Found", adServerDetail);
		}
		return new ServiceResponse(true, "Sucessfully fetch the active directory user ", adServerDetail);
	}

	/**
	 * Returns AES ecnrypted key to store in DB
	 * 
	 * @return
	 */
	private String encryptStringForDb(String plainText) {
		String encryptedString = aesEncryptionService.encrypt(plainText, customApiConfig.getAesEncryptionKey());
		return encryptedString == null ? "" : encryptedString;
	}

	/**
	 * gets Active Director Server configurations from DB
	 *
	 * @return ADserverDetails
	 */
	@Override
	public ADServerDetail getADServerConfig() {
		List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
		GlobalConfig globalConfig = CollectionUtils.isEmpty(globalConfigs) ? null : globalConfigs.get(0);
		ADServerDetail adServerDetail = globalConfig == null ? null : globalConfig.getAdServerDetail();
		if (adServerDetail != null) {
			String connectionUrl = ldapsConnection.concat(adServerDetail.getHost());
			adServerDetail.setHost(connectionUrl);
			String userPattern = userIdRegex.concat(adServerDetail.getRootDn());
			adServerDetail.setUserDn(userPattern);
			String plainString = decryptKey(adServerDetail.getPassword());
			adServerDetail.setPassword(plainString);
		}
		return adServerDetail;
	}

	/**
	 * Decrypts the password stored in DB
	 * 
	 * @param encryptedKey
	 * @return decryptedText
	 */
	private String decryptKey(String encryptedKey) {
		return aesEncryptionService.decrypt(encryptedKey, customApiConfig.getAesEncryptionKey());
	}
}
