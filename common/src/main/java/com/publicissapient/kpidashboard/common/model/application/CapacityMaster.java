package com.publicissapient.kpidashboard.common.model.application;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author narsingh9
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CapacityMaster {
	private ObjectId id;
	private String projectNodeId;
	private String projectName;
	private String sprintNodeId;
	private String sprintName;
	private String sprintState;
	private Double capacity;
	private String startDate; // format yyyy-mm-dd
	private String endDate; // format yyyy-mm-dd
	private ObjectId basicProjectConfigId; // not passed from UIL
	private List<Assignee> assignee;
	private boolean kanban;
	private boolean assigneeDetails;
}
