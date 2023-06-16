package com.publicissapient.kpidashboard.apis.bamboo.model;

import lombok.*;

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
