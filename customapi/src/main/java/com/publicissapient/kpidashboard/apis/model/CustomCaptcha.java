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

package com.publicissapient.kpidashboard.apis.model;

/**
 * Model class to hold captcha details
 * 
 * @author prijain3
 *
 */
public class CustomCaptcha {

	private String result;
	private byte[] image;
	private boolean captchaRequired;

	/**
	 * get result
	 *
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * set result
	 *
	 * @param result
	 *            the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * get image
	 * 
	 * @return the image
	 */
	public byte[] getImage() {
		return image == null ? null : image.clone();
	}

	/**
	 * set image
	 * 
	 * @param image
	 *            the image to set
	 */
	public void setImage(byte[] image) {
		this.image = image == null ? null : image.clone();
	}

	/**
	 * get captchaRequired
	 *
	 * @return captchaRequired
	 */
	public boolean isCaptchaRequired() {
		return captchaRequired;
	}

	/**
	 * set captchaRequired
	 *
	 * @param captchaRequired
	 *            the captchaRequired to set
	 */
	public void setCaptchaRequired(boolean captchaRequired) {
		this.captchaRequired = captchaRequired;
	}

}
