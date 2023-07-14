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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Service
@Slf4j
public class RsaEncryptionService implements EncryptionService {

	private static final String ALGORITHM = "RSA";

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
			Key pubKey = decodePublicKey(key);
			byte[] contentBytes = text.getBytes();
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			byte[] cipherContent = cipher.doFinal(contentBytes);
			return Base64.getEncoder().encodeToString(cipherContent);
		} catch (NoSuchAlgorithmException e) {
			log.error("Encryption - No such algorithm", e);
		} catch (InvalidKeyException e) {
			log.error("Encryption - invalid key", e);
		} catch (NoSuchPaddingException e) {
			log.error("Encryption - No such padding", e);
		} catch (BadPaddingException e) {
			log.error("Encryption - Bad padding", e);
		} catch (InvalidKeySpecException e) {
			log.error("Encryption - Invalid Key spec", e);
		} catch (IllegalBlockSizeException e) {
			log.error("Encryption - Illegal block size", e);
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
			log.error("Provide a key for decrypt");
			return null;
		}
		try {
			Key privKey = decodePrivateKey(key);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			byte[] cipherContentBytes = Base64.getDecoder().decode(encryptedText.getBytes());
			byte[] decryptedContent = cipher.doFinal(cipherContentBytes);
			return new String(decryptedContent);

		} catch (NoSuchAlgorithmException e) {
			log.error("Decryption - No such algorithm", e);
		} catch (InvalidKeyException e) {
			log.error("Decryption - invalid key", e);
		} catch (NoSuchPaddingException e) {
			log.error("Decryption - No such padding", e);
		} catch (BadPaddingException e) {
			log.error("Decryption - Bad padding", e);
		} catch (InvalidKeySpecException e) {
			log.error("Decryption - Invalid Key spec", e);
		} catch (IllegalBlockSizeException e) {
			log.error("Decryption - Illegal block size", e);
		}
		return null;
	}

	private PublicKey decodePublicKey(String keyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = Base64.getDecoder().decode(keyStr);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		return keyFactory.generatePublic(spec);

	}

	private PrivateKey decodePrivateKey(String keyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = Base64.getDecoder().decode(keyStr);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		return keyFactory.generatePrivate(keySpec);
	}

	public Map<String, String> generateKeys() {

		Map<String, String> keys = new HashMap<>();
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048, new SecureRandom());
			KeyPair pair = generator.generateKeyPair();
			keys.put("public", Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
			keys.put("private", Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
		} catch (NoSuchAlgorithmException e) {
			log.error("Error in generating keys", e);
		}
		return keys;
	}
}
