package com.publicissapient.kpidashboard.common.model.application.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ProjectAssigneeRolesDataDTO {
	private String roleName;
	private String roleDisplayName;
	private Date createdDate;
}
