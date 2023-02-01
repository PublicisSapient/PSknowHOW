package com.publicissapient.kpidashboard.apis.pushdata.model.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PushDeploy extends BuildDeploy {
	@NotNull(message = "The envName must not be null.")
	private String envName;

	@NotNull(message = "The deploymentStatus must not be null.")
	private String deploymentStatus;
}
