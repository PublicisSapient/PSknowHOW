package com.publicissapient.kpidashboard.apis.bamboo.model;

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
public class BambooBranchesResponseDTO {

	private String jobBranchKey;
	private String branchName;
}
