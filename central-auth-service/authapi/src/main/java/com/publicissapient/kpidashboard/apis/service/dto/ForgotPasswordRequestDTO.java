package com.publicissapient.kpidashboard.apis.service.dto;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {

	/** email id, must not be empty */
	@NotEmpty
	private String email;
}
