package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
	private ObjectId basicProjectConfigId;
	private List<AssigneeCapacity> assigneeCapacity;
	private boolean kanban;
	private boolean assigneeDetails;
}
