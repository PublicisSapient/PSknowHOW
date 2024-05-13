package com.publicissapient.kpidashboard.apis.auth.service;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserNameRequest {
	@NotNull
	private String userName;
}
