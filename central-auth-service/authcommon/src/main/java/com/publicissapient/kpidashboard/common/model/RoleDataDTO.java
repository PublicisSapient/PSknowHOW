package com.publicissapient.kpidashboard.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDataDTO {
	private Long id;
	private String name;
	private String description;
	private boolean rootUser;
	private boolean defaultRole;
	private String resource;

	public RoleDataDTO(Long id, String name) {
		this.id = id;
		this.name = name;
	}
}
