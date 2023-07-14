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

package com.publicissapient.kpidashboard.apis.appsetting.service;

import org.springframework.web.multipart.MultipartFile;

import com.publicissapient.kpidashboard.apis.model.BaseResponse;
import com.publicissapient.kpidashboard.apis.model.Logo;

/**
 * Interface managing all requests to the Excel based MVP on executive dash
 * board.
 *
 * @author pkum34
 *
 */
public interface FileStorageService {
	/**
	 * Uploads image file as log
	 * 
	 * @param file
	 * @return BaseResponse
	 * 
	 */
	BaseResponse upload(MultipartFile file);

	/**
	 * Uploads image file as log
	 *
	 * @param file
	 * @return BaseResponse
	 *
	 */
	BaseResponse upload(String type, MultipartFile file);

	/**
	 * Gets logo image
	 * 
	 * @return lOGO
	 */
	Logo getLogo();

	/**
	 * Delete logo image
	 * 
	 * @return boolean
	 */
	boolean deleteLogo();

}
