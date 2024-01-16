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

package com.publicissapient.kpidashboard.apis.auth.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.util.AESEncryption;

import lombok.extern.slf4j.Slf4j;

/**
 * Thi service validates captcha input text with the actual result
 * 
 * @author sgoe17
 *
 */
@Slf4j
@Service
public class CaptchaValidationServiceImpl implements CaptchaValidationService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sapient.customdashboard.service.CaptchaValidationService#
	 * validateCaptcha( java.lang.String, java.lang.String)
	 */

	@Autowired
	CustomApiConfig customApiConfig;

	@Override
	public boolean validateCaptcha(String encryptedString, String result) {

		boolean returnResult = false;
		String resultDecrypted = "";
		if (null != encryptedString) {

			try {
				resultDecrypted = AESEncryption.decrypt(encryptedString, customApiConfig.getAesKeyValue());
			} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
					| IllegalBlockSizeException exception) {
				log.error("Error while decryption", exception);
			}

		}

		if (resultDecrypted.equals(result)) {
			returnResult = true;
		}

		return returnResult;
	}

}
