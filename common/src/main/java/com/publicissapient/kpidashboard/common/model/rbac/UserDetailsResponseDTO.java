package com.publicissapient.kpidashboard.common.model.rbac;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * classes for userDetails for profile screen
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponseDTO {

	@JsonProperty("user_name")
	private String userName;

	@JsonProperty("user_email")
	private String userEmail;

	@JsonProperty("authorities")
	private List<String> authorities;

	@JsonProperty("projectsAccess")
	private List<RoleWiseProjects> projectsAccess;
}
