package com.publicissapient.kpidashboard.apis.common.service;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifierDTO;

public interface MetaDataIdentifierService {

	List<MetadataIdentifierDTO> getTemplateDetails();

	List<MetadataIdentifier> getMetaDataList();
}
