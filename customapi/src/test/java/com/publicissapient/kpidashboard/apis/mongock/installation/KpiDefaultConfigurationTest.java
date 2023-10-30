package com.publicissapient.kpidashboard.apis.mongock.installation;

import com.mongodb.client.MongoCollection;
import com.publicissapient.kpidashboard.apis.data.MetaDataIdentifierDataFactory;
import com.publicissapient.kpidashboard.apis.util.MongockUtil;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpiDefaultConfigurationTest {
	private static final String METADATA_IDENTIFIER_COLLECTION = "metadata_identifier";

	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private KpiDefaultConfiguration kpiDefaultConfiguration;
	@Mock
	private MongoCollection<Document> collection;

	@BeforeEach
	public void setUp() {
		MetaDataIdentifierDataFactory metaDataIdentifierDataFactory = MetaDataIdentifierDataFactory.newInstance();
		List<MetadataIdentifier> metadataIdentifierList = metaDataIdentifierDataFactory.getMetadataIdentifierList();
		kpiDefaultConfiguration = new KpiDefaultConfiguration(mongoTemplate);
		kpiDefaultConfiguration.metadataIdentifierList = metadataIdentifierList;
	}

	@Test
     void testChangeSet() {
        when(MongockUtil.getOrCreateCollection(mongoTemplate,METADATA_IDENTIFIER_COLLECTION)).thenReturn(collection);
        when(collection.countDocuments()).thenReturn(0L);
        assertTrue(kpiDefaultConfiguration.changeSet());

    }

}