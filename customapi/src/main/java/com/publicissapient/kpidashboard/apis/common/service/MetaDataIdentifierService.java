package com.publicissapient.kpidashboard.apis.common.service;

import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifierDTO;

import java.util.List;

public interface MetaDataIdentifierService {

    List<MetadataIdentifierDTO> getTemplateNamesAndID();

    List<MetadataIdentifier> getMetaDataList() ;
}
