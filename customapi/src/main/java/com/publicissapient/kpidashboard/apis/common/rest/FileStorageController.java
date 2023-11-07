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

package com.publicissapient.kpidashboard.apis.common.rest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.publicissapient.kpidashboard.apis.appsetting.service.FileStorageService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.BaseResponse;
import com.publicissapient.kpidashboard.apis.model.Logo;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.util.ValidExtension;

import lombok.extern.slf4j.Slf4j;

/**
 * REST service managing all requests to File storage utilities
 *
 * @author pkum34
 *
 */
@Validated
@RestController
@Slf4j
public class FileStorageController {

	private final FileStorageService fileStorageService;

	@Autowired
	public CustomApiConfig customApiConfig;

	@Autowired
	public FileStorageController(FileStorageService fileStorageService) {
		this.fileStorageService = fileStorageService;
	}

	/**
	 * Uploads image file as logo
	 * 
	 * @param file
	 * @return BaseResponse with <tt>message</tt> and
	 *         <tt>success status(true or false)</tt> of upload
	 */
	@PostMapping("/file/upload")
	@PreAuthorize("hasPermission('LOGO', 'FILE_UPLOAD')")
	public BaseResponse uploadImage(@ValidExtension @RequestParam("file") MultipartFile file) {
		return fileStorageService.upload(file);
	}

	/**
	 * Uploads image file as logo
	 * 
	 * @param file
	 * @return BaseResponse with <tt>message</tt> and
	 *         <tt>success status(true or false)</tt> of upload
	 */
	@PostMapping("/file/upload/{type}")
	@PreAuthorize("hasPermission(#type, 'FILE_UPLOAD')")
	public BaseResponse uploadFile(@PathVariable String type, @RequestParam("file") MultipartFile file) {
		return fileStorageService.upload(type, file);
	}

	/**
	 * Gets logo image file
	 * 
	 * @return Logo
	 */
	@GetMapping("/file/logo")
	public Logo getLogo() {

		return fileStorageService.getLogo();
	}

	/**
	 * Deletes the logo image
	 * 
	 * @return boolean
	 */
	@GetMapping("/file/delete")
	@PreAuthorize("hasPermission('LOGO', 'DELETE_LOGO')")
	public boolean deleteLogo() {
		return fileStorageService.deleteLogo();
	}

	@PostMapping("/file/uploadCertificate")
	public ResponseEntity<ServiceResponse> uploadCertificate(@RequestParam("file") MultipartFile file) {
		ServiceResponse response = new ServiceResponse(false, "LDAP certificate not copied due to some error",
				file.getOriginalFilename());

		String extension = file.getOriginalFilename();
		// Validate the file type
		if (!isValidFile(extension)) {
			response.setMessage("Invalid file type. Please upload a .cer file.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

		String fileName = extension.replace(".cer", "") + "_" + timestamp + ".cer";
		File dest = new File(customApiConfig.getHostPath(), fileName);
		try {
			dest.getParentFile().mkdirs();
			file.transferTo(dest);
			response.setSuccess(true);
			response.setMessage(
					"LDAP certificate copied successfully, please restart the customapi container service.");
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
		}
	}

	private boolean isValidFile(String extension) {

		boolean isValidFileExtension = false;
		try {
			isValidFileExtension = (null != extension) && (extension.endsWith(".cer"));

		} catch (Exception e) {
			log.error("Uploded File is either null or in incorrect format");
		}
		return isValidFileExtension;
	}

}
