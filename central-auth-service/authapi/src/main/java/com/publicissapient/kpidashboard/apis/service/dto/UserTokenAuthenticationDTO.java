package com.publicissapient.kpidashboard.apis.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserTokenAuthenticationDTO {

	private String username;
	private String email;
	private String authToken;
}
