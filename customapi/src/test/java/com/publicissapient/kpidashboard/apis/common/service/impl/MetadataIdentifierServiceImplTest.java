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