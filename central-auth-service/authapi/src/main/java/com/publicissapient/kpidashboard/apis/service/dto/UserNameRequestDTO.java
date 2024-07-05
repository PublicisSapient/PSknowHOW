package com.publicissapient.kpidashboard.apis.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import jakarta.validation.constraints.NotNull;


@Getter
@AllArgsConstructor
public class UserNameRequestDTO {
	@NotNull
	private String username;
}
