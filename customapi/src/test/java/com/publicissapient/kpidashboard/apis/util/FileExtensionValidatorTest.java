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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

import javax.validation.ConstraintValidatorContext;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FileExtensionValidatorTest {

    @InjectMocks
    private FileExtensionValidator fileExtensionValidator;

    @Test
    public void testValidFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        fileExtensionValidator.isValid(file, Mockito.mock(ConstraintValidatorContext.class));
    }

    @Test
    public void testInvalidFileExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());
        assertFalse(fileExtensionValidator.isValid(file, Mockito.mock(ConstraintValidatorContext.class)));
    }

    @Test
    public void testInvalidFileFormat() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "invalid-content".getBytes());
        assertFalse(fileExtensionValidator.isValid(file, Mockito.mock(ConstraintValidatorContext.class)));
    }

    @Test
    public void testInvalidFileSize() {
        // Adjust MAX_FILE_SIZE to a lower value for testing
        //FileExtensionValidator.MAX_FILE_SIZE = 10L;
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        assertFalse(fileExtensionValidator.isValid(file, Mockito.mock(ConstraintValidatorContext.class)));
    }
}