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

package com.publicissapient.kpidashboard.common.util;

import javax.xml.bind.DatatypeConverter;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
/**
 * Handles Encryption for the applications.
 */
public final class Encryption {

	private static final String ALGO = "AES";
	private static final String DEFAULT_MODE_AND_PADDING_SCHEME = "AES/GCM/PKCS5Padding";

	private Encryption() {
		// util class.
	}

	/**
	 * Gets string key.
	 *
	 * @return the string key
	 * @throws EncryptionException
	 *             the encryption exception
	 */
	public static String getStringKey() throws EncryptionException {
		SecretKey key = null;
		try {
			 key = KeyGenerator.getInstance(ALGO).generateKey();
		} catch (NoSuchAlgorithmException e) {
			throw new EncryptionException("Cannot generate a secret key" + '\n' + e.getMessage(), e);
		}
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	/**
	 * Gets aes encryption key.
	 *
	 * @param key
	 *            the key
	 * @return the aes encryption key
	 */
	private static SecretKey getAesEncryptionKey(String key) {
		// decode the base64 encoded string

		byte[] decodedKey = toByteArray(key);
		// rebuild key using SecretKeySpec
		return new SecretKeySpec(decodedKey, ALGO);
	}

	private static String bytesToHex(byte[] hash) {
		return DatatypeConverter.printHexBinary(hash);
	}

	/**
	 * To byte array byte [ ].
	 *
	 * @param str
	 *            the str
	 * @return the byte [ ]
	 */
	private static byte[] toByteArray(String str) {
		return DatatypeConverter.parseHexBinary(str);
	}

	/**
	 * Encrypts plainText in AES using the secret key
	 *
	 * @param plainText
	 *            the plain text
	 * @param key
	 *            the key
	 * @return string
	 * @throws NoSuchPaddingException
	 *             the no such padding exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws BadPaddingException
	 *             the bad padding exception
	 * @throws IllegalBlockSizeException
	 *             the illegal block size exception
	 * @throws InvalidKeyException
	 *             the invalid key exception
	 */
	public static String aesEncryptString(String plainText, String key) throws NoSuchPaddingException,
			NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
		SecretKey secKey = getAesEncryptionKey(key);

		// AES defaults to AES/CBC/PKCS5Padding in Java 7
		 Cipher aesCipher = Cipher.getInstance(DEFAULT_MODE_AND_PADDING_SCHEME); // NOSONAR
		 aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
		byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
		return bytesToHex(byteCipherText);
	}

	/**
	 * Aes decrypt string string.
	 *
	 * @param byteCipherText
	 *            the byte cipher text
	 * @param key
	 *            the key
	 * @return the string
	 * @throws NoSuchPaddingException
	 *             the no such padding exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws InvalidKeyException
	 *             the invalid key exception
	 * @throws BadPaddingException
	 *             the bad padding exception
	 * @throws IllegalBlockSizeException
	 *             the illegal block size exception
	 */
	public static String aesDecryptString(String byteCipherText, String key) throws GeneralSecurityException {
		SecretKey secKey = getAesEncryptionKey(key);
		Cipher aesCipher;
		try {
			// Use AES/CBC/PKCS5Padding instead of the default algorithm and padding scheme
			aesCipher = Cipher.getInstance(DEFAULT_MODE_AND_PADDING_SCHEME);// NOSONAR
			aesCipher.init(Cipher.DECRYPT_MODE, secKey);
			byte[] byteCipherString = toByteArray(byteCipherText);
			byte[] bytePlainText = aesCipher.doFinal(byteCipherString);
			return new String(bytePlainText);
		} catch (GeneralSecurityException e) {
			throw new GeneralSecurityException("Could not retrieve AES cipher", e);
		}

	}

}