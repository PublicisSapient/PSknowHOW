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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.BaseResponse;
import com.publicissapient.kpidashboard.apis.model.Logo;
import com.publicissapient.kpidashboard.apis.model.MultiPartFileDTO;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

/**
 * class managing all requests to the Excel based MVP on executive dash board.
 *
 * @author pkum34
 *
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageServiceImpl.class);
	public static final String STR_CAPACITY = "CAPACITY";
	public static final String STR_TESTEXECUTION = "TEST_EXECUTION";
	public static final String STR_KANBAN_CAPACITY = "KANBAN_CAPACITY";
	public static final String FILE_NAME = "filename";
	public static final String UPLOAD_SUCCESS = "File uploaded successfully";
	public static final String UPLOAD_FAIL = "Upload failed : ";
	private static final String INVALID_FILE_CODE = "201";
	private static final String INVALID_UPLOAD_TYPE = "202";
	private static final String FILE_SAVE_ERROR = "203";

	@Autowired
	private GridFsOperations gridOperations;

	@Autowired
	CustomApiConfig customApiConfig;


	private static final ModelMapper modelMapper = new ModelMapper();

	/**
	 * Uploads <tt>Logo</tt> file
	 * 
	 * @param multifile
	 * ``
	 * @return baseResponse with success message if file uploads successfully.
	 */
	@Override
	public BaseResponse upload(MultipartFile multifile) {
		BaseResponse baseResponse = new BaseResponse();
		MultiPartFileDTO multipartFile = new MultiPartFileDTO();
		try {
			multipartFile = modelMapper.map(multifile, MultiPartFileDTO.class);
			writeToFile(multipartFile.getOriginalFilename(), multipartFile.getBytes());
		} catch (IOException e) {

			LOGGER.error(UPLOAD_FAIL, e);
		}
		try (InputStream imageInputStream = Files.newInputStream(Paths.get(multipartFile.getOriginalFilename()))) {
			DBObject metaData = new BasicDBObject();
			String fileName = Constant.LOGO_FIL_NAME;
			metaData.put("type", "image");

			gridOperations.delete(new Query().addCriteria(Criteria.where(FILE_NAME).is(fileName)));

			gridOperations.store(imageInputStream, fileName, "image/png", metaData);

			LOGGER.info(UPLOAD_SUCCESS);
			baseResponse.setMessage(UPLOAD_SUCCESS);
		} catch (IOException exeption) {
			LOGGER.error(UPLOAD_FAIL, exeption);
		}

		return baseResponse;
	}

	@Override
	public ServiceResponse upload(String type, MultipartFile userMultipartFile) {
		try {
			MultiPartFileDTO multipartFile = modelMapper.map(userMultipartFile, MultiPartFileDTO.class);
			if (!isValidFile(multipartFile)) {
				return new ServiceResponse(false, "Invalid file", INVALID_FILE_CODE);
			}
			String filePath = "";
			switch (type) {

			default:
				break;
			}
			if (StringUtils.isEmpty(filePath)) {
				return new ServiceResponse(false, "Invalid upload type", INVALID_UPLOAD_TYPE);
			}

			writeToFile(filePath, multipartFile.getBytes());

			return new ServiceResponse(true, UPLOAD_SUCCESS, null);
		} catch (IOException e) {
			LOGGER.error(UPLOAD_FAIL, e);
			return new ServiceResponse(false, "Error in saving the file on disk", FILE_SAVE_ERROR);
		}
	}

	private boolean isValidFile(MultiPartFileDTO multipartFile) {
		String extension = multipartFile.getOriginalFilename();
		boolean isValidFileExtension = (null != extension)
				&& (extension.endsWith(".xlsx") || extension.endsWith(".XLSX"));
		boolean isValidFormat = false;
		try {
			isValidFormat = true;
		} catch (Exception e) {
			LOGGER.error("Excel format error ", e);
		}
		return isValidFileExtension && isValidFormat;
	}

	/**
	 * Writes to file from <tt>content</tt>
	 * 
	 * @param fileName
	 * @param content
	 * @throws IOException
	 */
	public void writeToFile(String fileName, byte[] content) throws IOException {
		try (OutputStream os = Files.newOutputStream(Paths.get(fileName))) {
			os.write(content);
		}
	}

	/**
	 * Gets Logo from MongoDB GridFS which matches with the <tt>FILE_NAME</tt>
	 * 
	 * @return logo
	 */
	@Override
	public Logo getLogo() {
		Logo logo = new Logo();
		String fileName = Constant.LOGO_FIL_NAME;

		GridFSFindIterable gridFSFindIterable = gridOperations
				.find(new Query().addCriteria(Criteria.where(FILE_NAME).is(fileName)));

		gridFSFindIterable.forEach((Consumer<? super GridFSFile>) file -> {
			try {
				InputStream iStream = gridOperations.getResource(file).getInputStream();
				byte[] bytes = IOUtils.toByteArray(iStream);
				logo.setImage(bytes);
			} catch (IOException ioException) {

				LOGGER.error("Exception while writing logo image:", ioException);

			}

		});


		return logo;
	}

	/**
	 * Deletes <tt>Logo</tt> file which matches <tt>FILE_NAME</tt> in the query
	 * 
	 * @return true after Logo is deleted
	 */
	@Override
	public boolean deleteLogo() {
		String fileName = Constant.LOGO_FIL_NAME;
		gridOperations.delete(new Query().addCriteria(Criteria.where(FILE_NAME).is(fileName)));
		return true;
	}

}