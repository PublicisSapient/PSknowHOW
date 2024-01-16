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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.CustomCaptcha;
import com.publicissapient.kpidashboard.apis.util.AESEncryption;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.Captcha.Builder;
import cn.apiclub.captcha.backgrounds.FlatColorBackgroundProducer;
import cn.apiclub.captcha.text.producer.DefaultTextProducer;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * This service produces a Captcha image with a random string
 * 
 * @author sgoe17
 *
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

	@Autowired
	private CustomApiConfig customApiConfig;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sapient.customdashboard.service.CaptchaService#getCaptcha()
	 */
	@Override
	public CustomCaptcha getCaptcha() {

		CustomCaptcha responseCaptcha = new CustomCaptcha();

		if (null != customApiConfig) {
			responseCaptcha.setCaptchaRequired(customApiConfig.isCaptchaRequired());
		}

		DefaultTextProducer producer = new DefaultTextProducer();
		Builder builder = new Captcha.Builder(150, 45);
		builder.addText(producer);
		builder.addBackground(new FlatColorBackgroundProducer(Color.white));
		builder.addBorder();
		builder.addNoise();
		Captcha captcha = builder.build();
		BufferedImage image = captcha.getImage();

		String result = captcha.getAnswer();
		byte[] imageInByte = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "jpg", baos);
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();
		} catch (IOException ioException) {
			log.error("Error while encryption", ioException);
		}

		responseCaptcha.setImage(imageInByte);

		String encryptedResult = "";

		try {
			encryptedResult = AESEncryption.encrypt(result, customApiConfig.getAesKeyValue());
		} catch (IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
				| BadPaddingException exception) {

			log.error("Error while encryption", exception);
		}

		responseCaptcha.setResult(encryptedResult);

		return responseCaptcha;
	}

}
