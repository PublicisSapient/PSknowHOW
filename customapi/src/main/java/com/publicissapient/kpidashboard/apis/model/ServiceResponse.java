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
 * Class used for common response from All services
 */
public class ServiceResponse extends BaseResponse {

	private Object data;

	/**
	 * 
	 * @param isSuccess
	 * @param msg
	 * @param data
	 */
	public ServiceResponse(Boolean isSuccess, String msg, Object data) {
		super();
		this.data = data;
		setMessage(msg);
		setSuccess(isSuccess);
	}

	/**
	 * 
	 * @return data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Sets data
	 * 
	 * @param data
	 */
	public void setData(Object data) {
		this.data = data;
	}

}
