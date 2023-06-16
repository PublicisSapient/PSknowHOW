package com.publicissapient.kpidashboard.common.model.sonar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Provide sonar version list based on type (sonarServer, SonarCloud) and
 * Branch supported or not
 *
 *
 * @author Hiren babariya
 *
 */

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SonarVersionResponseDTO {

	private String type;
	private boolean branchSupport;
	private List<String> versions;

}
