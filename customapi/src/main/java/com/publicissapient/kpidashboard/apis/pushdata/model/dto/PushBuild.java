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
public class PushBuild extends BuildDeploy {

	private String buildUrl;

	@NotNull(message = "The buildStatus must not be null.")
	private String buildStatus;
}
