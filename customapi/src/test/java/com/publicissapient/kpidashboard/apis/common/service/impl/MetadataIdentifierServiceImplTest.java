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

package com.publicissapient.kpidashboard.apis.common.service.impl;

import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifierDTO;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataIdentifierServiceImplTest {

    @InjectMocks
    private MetadataIdentifierServiceImpl metadataIdentifierService;

    @Mock
    private MetadataIdentifierRepository metadataIdentifierRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetMetaDataList() {
        MetadataIdentifier metadata1 = new MetadataIdentifier();
        MetadataIdentifier metadata2 = new MetadataIdentifier();
        List<MetadataIdentifier> mockData = Arrays.asList(metadata1, metadata2);
        when(metadataIdentifierRepository.findAll()).thenReturn(mockData);
        List<MetadataIdentifier> result = metadataIdentifierService.getMetaDataList();
        verify(metadataIdentifierRepository).findAll();
        assertEquals(mockData, result);
    }

    @Test
    public void testGetTemplateDetails() {
        MetadataIdentifier metadata1 = new MetadataIdentifier();
        metadata1.setTemplateName("template");
        metadata1.setId(new ObjectId());
        metadata1.setTemplateCode("code");
        metadata1.setIsKanban(false);
        metadata1.setTool("SCM");
        metadata1.setDisabled(false);
        List<MetadataIdentifier> mockData = Arrays.asList(metadata1);
        when(metadataIdentifierRepository.findAll()).thenReturn(mockData);
        List<MetadataIdentifierDTO> result = metadataIdentifierService.getTemplateDetails();
        verify(metadataIdentifierRepository).findAll();
        assertEquals(mockData.size(), result.size());
    }

}