package com.publicissapient.kpidashboard.apis.azure.model;

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
public class AzurePipelinesResponseDTO {

	private String pipelineName;
	private String definitions;
}
