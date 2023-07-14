package com.publicissapient.kpidashboard.apis.bamboo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BambooDeploymentProjectsResponseDTO {

	private String deploymentProjectName;
	private String deploymentProjectId;
}
