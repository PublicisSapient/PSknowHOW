package com.publicissapient.kpidashboard.common.model;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {

	/**
	 * email id, must not be empty
	 */
	@NotEmpty
	private String email;
}
