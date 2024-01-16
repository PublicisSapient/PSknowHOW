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

package com.publicissapient.kpidashboard.apis.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * This class provides methods to encrypt and decrypt String
 */
public final class AESEncryption {

	private static final String ALGO = "AES";

	/**
	 * Class constructor
	 */
	private AESEncryption() {
	}

	/**
	 * Encrypt String.
	 * 
	 * @param data
	 * @return encrypted string
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static String encrypt(String data, List<Character> aesKeyValue) throws IllegalBlockSizeException, BadPaddingException,
			InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		Key key = generateKey(aesKeyValue);
		Cipher cipher = Cipher.getInstance(ALGO);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = cipher.doFinal(data.getBytes());
		return Base64.encodeBase64String(encVal);
	}

	/**
	 * Decrypt encrypted string
	 * 
	 * @param encryptedData
	 * @return decrypted string
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static String decrypt(String encryptedData, List<Character> aesKeyValue) throws IllegalBlockSizeException, BadPaddingException,
			InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		Key key = generateKey(aesKeyValue);
		Cipher cipher = Cipher.getInstance(ALGO);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decordedValue = Base64.decodeBase64(encryptedData);
		byte[] decValue = cipher.doFinal(decordedValue);
		return new String(decValue);
	}

	private static Key generateKey(List<Character> aesKeyValue) {
		byte[] byteArray = new byte[aesKeyValue.size()];
		aesKeyValue.forEach(character -> byteArray[aesKeyValue.indexOf(character)] = (byte) character.charValue());
		return new SecretKeySpec(byteArray, ALGO);
	}
}