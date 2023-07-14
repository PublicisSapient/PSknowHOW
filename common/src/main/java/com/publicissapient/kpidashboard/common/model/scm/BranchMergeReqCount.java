package com.publicissapient.kpidashboard.common.model.scm;

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
public class BranchMergeReqCount {

	private String branchName;
	private List<MergeReqCount> weekWiseData;

}
