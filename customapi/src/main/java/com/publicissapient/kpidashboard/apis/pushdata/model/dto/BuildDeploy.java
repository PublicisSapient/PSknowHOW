package com.publicissapient.kpidashboard.apis.pushdata.model.dto;

import javax.validation.constraints.Min;
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
public class BuildDeploy {
	@NotNull(message = "The jobName must not be null.")
	private String jobName;
	@NotNull(message = "The number must not be null.")
	private String number;

	@NotNull(message = "The startTime must not be null.")
	@Min(value = 0, message = "The startTime must be positive.")
	private Long startTime;

	@NotNull(message = "The endTime must not be null.")
	@Min(value = 0, message = "The endTime must be positive.")
	private Long endTime;

	@NotNull(message = "The duration must not be null.")
	@Min(value = 0, message = "The duration must be positive.")
	private Long duration;
}
