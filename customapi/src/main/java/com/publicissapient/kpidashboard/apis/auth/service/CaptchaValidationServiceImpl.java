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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.util.AESEncryption;

/**
 * Thi service validates captcha input text with the actual result
 * 
 * @author sgoe17
 *
 */
@Service
public class CaptchaValidationServiceImpl implements CaptchaValidationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CaptchaValidationServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sapient.customdashboard.service.CaptchaValidationService#
	 * validateCaptcha( java.lang.String, java.lang.String)
	 */

	@Override
	public boolean validateCaptcha(String encryptedString, String result) {

		boolean returnResult = false;
		String resultDecrypted = "";
		if (null != encryptedString) {

			try {
				resultDecrypted = AESEncryption.decrypt(encryptedString);
			} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
					| BadPaddingException | IllegalBlockSizeException exception) {
				LOGGER.error("Error while decryption", exception);
			}

		}

		if (resultDecrypted.equals(result)) {
			returnResult = true;
		}

		return returnResult;
	}

}
