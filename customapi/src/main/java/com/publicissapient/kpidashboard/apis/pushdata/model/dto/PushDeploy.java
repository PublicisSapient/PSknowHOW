package com.publicissapient.kpidashboard.apis.pushdata.model.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PushDeploy extends BuildDeployFields {
	@NotNull(message = "The environment must not be null.")
	@JsonProperty("envName")
	private String envName;

	@NotNull(message = "The deploymentStatus must not be null.")
	@JsonProperty("deploymentStatus")
	private String deploymentStatus;
}
