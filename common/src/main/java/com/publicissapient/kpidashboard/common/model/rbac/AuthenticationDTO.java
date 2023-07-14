package com.publicissapient.kpidashboard.common.model.rbac;

import lombok.Data;

@Data
public class AuthenticationDTO {
	private String username;
	private String email;
	private boolean approved;

}
