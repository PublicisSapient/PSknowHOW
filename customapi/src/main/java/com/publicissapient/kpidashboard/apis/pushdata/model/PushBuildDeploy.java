package com.publicissapient.kpidashboard.apis.pushdata.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuild;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushDeploy;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PushBuildDeploy {
	List<PushBuild> builds;
	List<PushDeploy> deployments;
}
