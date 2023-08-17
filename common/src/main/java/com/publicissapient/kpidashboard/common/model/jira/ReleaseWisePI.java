package com.publicissapient.kpidashboard.common.model.jira;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseWisePI {

	private String basicProjectConfigId;
	private List<String> releaseName;
	private String uniqueTypeName;
}
