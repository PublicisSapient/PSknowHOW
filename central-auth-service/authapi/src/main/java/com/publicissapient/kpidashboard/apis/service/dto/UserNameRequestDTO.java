package com.publicissapient.kpidashboard.apis.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserNameRequestDTO {
	@NotNull
	private String username;
}
