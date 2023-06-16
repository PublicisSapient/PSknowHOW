package com.publicissapient.kpidashboard.common.model.jira;

import org.bson.types.ObjectId;

import lombok.Data;

@Data
public class MetadataIdentifierDTO {

	private ObjectId id;
	private String tool;
	private String templateName;
	private String templateCode;
	private boolean isKanban;
	private boolean disabled;
}
