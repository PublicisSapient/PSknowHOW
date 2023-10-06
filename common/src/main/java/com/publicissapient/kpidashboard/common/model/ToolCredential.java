package com.publicissapient.kpidashboard.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolCredential {

	private String username;
	private String password;
	private String email;

}
