package com.publicissapient.kpidashboard.common.model.scm;

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
public class MergeReqCount {

	private String week;
	private Double time;
	private String unit;

}
