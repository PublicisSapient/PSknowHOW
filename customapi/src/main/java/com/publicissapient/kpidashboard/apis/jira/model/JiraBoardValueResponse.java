package com.publicissapient.kpidashboard.apis.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraBoardValueResponse {

	private long id;
	private String self;
	private String name;
	private String type;
}
