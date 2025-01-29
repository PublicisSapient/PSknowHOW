package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.MetaDataIdentifierService;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifierDTO;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;

@Service
public class MetadataIdentifierServiceImpl implements MetaDataIdentifierService {
	private static final String CUSTOM_TEMPLATE = "Custom Template";

	@Autowired
	private MetadataIdentifierRepository metadataIdentifierRepository;

	@Override
	public List<MetadataIdentifier> getMetaDataList() {
		return (List<MetadataIdentifier>) metadataIdentifierRepository.findAll();
	}

	@Override
	public List<MetadataIdentifierDTO> getTemplateDetails() {

		List<MetadataIdentifierDTO> templateNamesFlagAndID = new ArrayList<>();
		List<MetadataIdentifier> metadataIdentifierList = getNonCustomList();
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

	private List<MetadataIdentifier> getNonCustomList() {
		return getMetaDataList().stream()
				.filter(template -> StringUtils.isEmpty(template.getTemplateName()) || !template.getTemplateName().equalsIgnoreCase(CUSTOM_TEMPLATE)).toList();
	}

}
