package com.publicissapient.kpidashboard.apis.service.dto;

import lombok.Data;

import org.hibernate.validator.constraints.NotEmpty;

@Data
public class ForgotPasswordRequestDTO {

	/**
	 * email id, must not be empty
	 */
	@NotEmpty
	private String email;
}
