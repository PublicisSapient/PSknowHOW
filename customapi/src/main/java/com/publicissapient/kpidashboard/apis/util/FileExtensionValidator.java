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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.swing.*;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.publicissapient.kpidashboard.apis.model.MultiPartFileDTO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileExtensionValidator implements ConstraintValidator<ValidExtension, MultipartFile> {

	private static final Long MAX_FILE_SIZE = 51_200L;

	private static final ModelMapper modelMapper = new ModelMapper();

	@Override
	public void initialize(ValidExtension extension) {
		// no need to initialize anything
	}

	/**
	 * 
	 * <p>
	 * Validates <tt>file</tt> by checking the size,format and extension of the
	 * file.
	 * </p>
	 * 
	 * @param file
	 * @param context
	 * @return true if <tt>file</tt> size is greater than 0 and less than
	 *         <tt>MAX_FILE_SIZE</tt>
	 */
	@Override
	public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {

		boolean isValidFileFormat = false;
		boolean isValidFileExtension = false;
		boolean isValidSize = false;

		if (null != file) {

			try {
				MultiPartFileDTO multipartFile = modelMapper.map(file, MultiPartFileDTO.class);
				writeToFile(multipartFile.getOriginalFilename(), multipartFile.getBytes());

				byte[] iconByteArray = readFromFile(multipartFile.getOriginalFilename());

				ImageIcon icon = new ImageIcon(iconByteArray);
				BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
						BufferedImage.TYPE_INT_ARGB);
				icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
				isValidFileFormat = true;

				String extension = multipartFile.getOriginalFilename();
				isValidFileExtension = (null != extension) && (extension.endsWith(".png") || extension.endsWith(".PNG")
						|| extension.endsWith(".JPEG") || extension.endsWith(".jpeg") || extension.endsWith(".jpg")
						|| extension.endsWith(".JPG") || extension.endsWith(".gif") || extension.endsWith(".GIF")
						|| extension.endsWith(".bmp") || extension.endsWith(".BMP"));

				isValidSize = multipartFile.getSize() > 0 && multipartFile.getSize() <= MAX_FILE_SIZE;

			} catch (IllegalArgumentException | IOException exception) {
				isValidFileFormat = false;
				log.error("Error while uploading", exception);
			}
		}

		return isValidFileExtension && isValidFileFormat && isValidSize;

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
	 * Reads the the content from file
	 * 
	 * @param fileName
	 * @return the content of file in byte[]
	 * @throws IOException
	 */
	public byte[] readFromFile(String fileName) throws IOException {
		byte[] buf = new byte[8192];
		try (InputStream is = Files.newInputStream(Paths.get(fileName))) {
			int len = is.read(buf);
			if (len < buf.length) {
				return Arrays.copyOf(buf, len);
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream(16_384);
			while (len != -1) {
				os.write(buf, 0, len);
				len = is.read(buf);
			}
			return os.toByteArray();
		}
	}

}