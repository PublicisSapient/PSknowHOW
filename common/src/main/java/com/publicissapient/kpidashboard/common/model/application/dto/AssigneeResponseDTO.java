package com.publicissapient.kpidashboard.common.model.application.dto;

import java.util.List;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.AssigneeDetailsDTO;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class AssigneeResponseDTO extends BasicModel {
	private ObjectId basicProjectConfigId;
	private List<AssigneeDetailsDTO> assigneeDetailsList;
}
