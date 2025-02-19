package com.publicissapient.kpidashboard.common.model.jira;

import java.util.List;

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
public class ReleaseWisePI {

	private String basicProjectConfigId;
	private List<String> releaseName;
	private String uniqueTypeName;
}
