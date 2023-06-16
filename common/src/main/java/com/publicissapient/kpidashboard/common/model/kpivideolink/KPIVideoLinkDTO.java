package com.publicissapient.kpidashboard.common.model.kpivideolink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * id ObjectId in DB videoUrl URL
 *
 */

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KPIVideoLinkDTO {

	private String id;
	private String videoUrl;

}
