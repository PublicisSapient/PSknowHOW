package com.publicissapient.kpidashboard.apis.service.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;


@Data
public class UserNameRequestDTO {
	@NotNull
	private String username;
}
