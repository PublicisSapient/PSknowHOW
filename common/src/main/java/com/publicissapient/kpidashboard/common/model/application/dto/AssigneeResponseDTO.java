package com.publicissapient.kpidashboard.common.model.application.dto;

import com.publicissapient.kpidashboard.common.model.application.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@Setter
@Getter
public class AssigneeResponseDTO extends BasicModel {
    private String projectName;
    private ObjectId basicProjectConfigId;
    private List<AssigneeDetails> assigneeDetailsList;
}
