package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.MetaDataIdentifierService;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifierDTO;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;

@Service
public class MetadataIdentifierServiceImpl implements MetaDataIdentifierService {

	@Autowired
	private MetadataIdentifierRepository metadataIdentifierRepository;

	@Override
	public List<MetadataIdentifier> getMetaDataList() {
		return (List<MetadataIdentifier>) metadataIdentifierRepository.findAll();
	}

	@Override
	public List<MetadataIdentifierDTO> getTemplateDetails() {

		List<MetadataIdentifierDTO> templateNamesFlagAndID = new ArrayList<>();
		List<MetadataIdentifier> metadataIdentifierList = getMetaDataList();
		for (MetadataIdentifier metadataIdentifier : metadataIdentifierList) {
			MetadataIdentifierDTO metadataIdentifierDTO = new MetadataIdentifierDTO();
			metadataIdentifierDTO.setTemplateName(metadataIdentifier.getTemplateName());
			metadataIdentifierDTO.setId(metadataIdentifier.getId());
			metadataIdentifierDTO.setTemplateCode(metadataIdentifier.getTemplateCode());
			metadataIdentifierDTO.setKanban(metadataIdentifier.getIsKanban());
			metadataIdentifierDTO.setTool(metadataIdentifier.getTool());
			metadataIdentifierDTO.setDisabled(metadataIdentifier.isDisabled());
			templateNamesFlagAndID.add(metadataIdentifierDTO);
		}
		return templateNamesFlagAndID;
	}

}
