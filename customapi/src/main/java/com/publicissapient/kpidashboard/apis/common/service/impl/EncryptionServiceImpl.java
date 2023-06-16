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

package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.EncryptionService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.util.Encryption;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to provide implementation for encryption methods.
 * 
 * @author prijain3
 * 
 * @deprecated
 *
 */
@Service
@Slf4j
@Deprecated
public class EncryptionServiceImpl implements EncryptionService {

	private final CustomApiConfig apiSettings;

	@Autowired
	public EncryptionServiceImpl(final CustomApiConfig apiSettings) {
		this.apiSettings = apiSettings;
	}

	@Override
	public String encrypt(String message) {
		String key = apiSettings.getAesEncryptionKey();
		String returnString = "ERROR";
		if (!Constant.EMPTY_STRING.equals(key)) {

			try {
				returnString = Encryption.aesEncryptString(message, key);
			} catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException
					| InvalidKeyException exception) {
				log.error("Failed to encript messaege", exception);

			}

		}
		return returnString;
	}
}
