package com.publicissapient.kpidashboard.apis.repotools.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepoToolsStatusResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private String project;
	private String repository;
	private String source;
	private String status;
	private String timestamp;

}
