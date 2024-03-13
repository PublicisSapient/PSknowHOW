package com.publicissapient.kpidashboard.apis.repotools.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepoToolConnModel {
	private List<RepoToolConnectionDetail> item;
}
