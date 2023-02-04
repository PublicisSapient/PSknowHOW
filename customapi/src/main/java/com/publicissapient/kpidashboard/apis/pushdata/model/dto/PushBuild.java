package com.publicissapient.kpidashboard.apis.pushdata.model.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class PushBuild extends BuildDeployFields {
	@JsonProperty("buildUrl")
	private String buildUrl;

	@NotNull(message = "The buildStatus must not be null.")
	@JsonProperty("buildStatus")
	private String buildStatus;
}
