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

package com.publicissapient.kpidashboard.common.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Service
@Slf4j
public class AesEncryptionService implements EncryptionService {

	private static final String ALGO = "AES";
	private static final String DEFAULT_MODE_AND_PADDING_SCHEME = "AES/CBC/PKCS5Padding";
	private static final int ITERATION_COUNT = 65536;
	private static final int KEY_LENGTH = 256;
	private static final String KEY_INSTANCE = "PBKDF2WithHmacSHA1";

	@Override
	public String encrypt(String text, String key) {
		if (StringUtils.isEmpty(text)) {
			log.error("Provide some text to encrypt");
			return null;
		}
		if (StringUtils.isEmpty(key)) {
			log.error("Provide a key for encryption");
			return null;
		}

		try {
			byte[] salt = generateSalt();
			SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_INSTANCE);
			KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
			SecretKey tempKey = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tempKey.getEncoded(), ALGO);
			Cipher cipher = Cipher.getInstance(DEFAULT_MODE_AND_PADDING_SCHEME);// NOSONAR
			IvParameterSpec iv = generateIv();
			cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
			byte[] encryptedText = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.write(salt);
			outputStream.write(iv.getIV());
			outputStream.write(encryptedText);
			return Base64.getEncoder().encodeToString(outputStream.toByteArray());
		} catch (InvalidKeySpecException e) {
			log.error("Encryption - Invalid Key spec", e);
		} catch (NoSuchAlgorithmException e) {
			log.error("Encryption - No such algorithm", e);
		} catch (BadPaddingException e) {
			log.error("Encryption - Bad padding", e);
		} catch (InvalidKeyException e) {
			log.error("Encryption - Invalid key", e);
		} catch (InvalidAlgorithmParameterException e) {
			log.error("Encryption - Invalid algo params", e);
		} catch (NoSuchPaddingException e) {
			log.error("Encryption - No such padding", e);
		} catch (IllegalBlockSizeException e) {
			log.error("Encryption - Illegal block size", e);
		} catch (IOException io) {
			log.error("Encryption - Invalid I/O type", io);
		}
		return null;
	}

	@Override
	public String decrypt(String encryptedText, String key) {
		if (StringUtils.isEmpty(encryptedText)) {
			log.error("Provide some text to decrypt");
			return null;
		}
		if (StringUtils.isEmpty(key)) {
			log.error("Provide a key for decryption");
			return null;
		}
		byte[] cipherText = Base64.getDecoder().decode(encryptedText);
		if (cipherText.length < 48) {
			log.error("Decryption - Invalid cipher text. text is not of 48 byte long");
			return null;
		}
		byte[] salt = Arrays.copyOfRange(cipherText, 0, 16);
		byte[] iv = Arrays.copyOfRange(cipherText, 16, 32);
		byte[] ct = Arrays.copyOfRange(cipherText, 32, cipherText.length);
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_INSTANCE);
			KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
			SecretKey tempKey = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tempKey.getEncoded(), ALGO);
			Cipher cipher = Cipher.getInstance(DEFAULT_MODE_AND_PADDING_SCHEME);// NOSONAR
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);
			byte[] original = cipher.doFinal(ct);

			return new String(original, StandardCharsets.UTF_8);
		} catch (InvalidKeySpecException e) {
			log.error("Decryption - Invalid Key spec", e);
		} catch (NoSuchAlgorithmException e) {
			log.error("Decryption - No such algorithm", e);
		} catch (BadPaddingException e) {
			log.error("Decryption - Bad padding", e);
		} catch (InvalidKeyException e) {
			log.error("Decryption - Invalid key", e);
		} catch (InvalidAlgorithmParameterException e) {
			log.error("Decryption - Invalid algo params", e);
		} catch (NoSuchPaddingException e) {
			log.error("Decryption - No such padding", e);
		} catch (IllegalBlockSizeException e) {
			log.error("Decryption - Illegal block size", e);
		}
		return null;
	}

	private IvParameterSpec generateIv() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		return new IvParameterSpec(iv);
	}

	private byte[] generateSalt() {
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);
		return salt;
	}

}
