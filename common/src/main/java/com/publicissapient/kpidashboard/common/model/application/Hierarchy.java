package com.publicissapient.kpidashboard.common.model.application;

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
public class Hierarchy {

	private HierarchyLevel hierarchyLevel;
	private String value;
}
