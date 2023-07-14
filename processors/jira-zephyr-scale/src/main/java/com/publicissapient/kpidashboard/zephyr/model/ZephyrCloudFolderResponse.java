package com.publicissapient.kpidashboard.zephyr.model;

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
public class ZephyrCloudFolderResponse {

	private String id;
	private String parentId;
	private String name;
}
