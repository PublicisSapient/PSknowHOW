package com.publicissapient.kpidashboard.jira.service;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CreateAssigneeDetailsImpl implements CreateAssigneeDetails {

    @Autowired
    private AssigneeDetailsRepository assigneeDetailsRepository;

    @Override
    public AssigneeDetails createAssigneeDetails(ProjectConfFieldMapping projectConfig, Set<Assignee> assigneeSetToSave) {
        AssigneeDetails assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(
                projectConfig.getBasicProjectConfigId().toString(), ProcessorConstants.JIRA);
        if (CollectionUtils.isNotEmpty(assigneeSetToSave)) {
            if (assigneeDetails == null) {
                assigneeDetails = new AssigneeDetails();
                assigneeDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
                assigneeDetails.setSource(ProcessorConstants.JIRA);
                assigneeDetails.setAssignee(assigneeSetToSave);
            } else {
                Set<Assignee> updatedAssigneeSetToSave = new HashSet<>();
                updatedAssigneeSetToSave.addAll(assigneeDetails.getAssignee());
                updatedAssigneeSetToSave.addAll(assigneeSetToSave);
                assigneeDetails.setAssignee(updatedAssigneeSetToSave);
            }
//            assigneeDetailsRepository.save(assigneeDetails);
        }
        return assigneeDetails;
    }
}
