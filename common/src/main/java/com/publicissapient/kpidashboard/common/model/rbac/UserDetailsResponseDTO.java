package com.publicissapient.kpidashboard.common.model.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

	private String userName;

	private String userEmail;

	private List<String> authorities;

	private List<RoleWiseProjects> projectsAccess;
}
